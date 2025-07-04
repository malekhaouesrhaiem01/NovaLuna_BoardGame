package service


import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import tools.aqua.bgw.util.Coordinate


/**
 * Service layer class that realizes the necessary logic for sending and receiving Nova Luna network messages.
 * Bridges between the [NovaLunaNetworkClient] and the other services.
 *
 * @param rootService The [RootService] instance to access game logic and the entity layer
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {


    /** What *this* client chose — HUMAN, EASYBOT, or HARDBOT */
    var myPlayerType: PlayerType = PlayerType.HUMAN

    /** Tracks host + guest names in join order */
    val currentSessionPlayers = mutableListOf<String>()


    /** Underlying BGW-Net client; null when offline */
    private var client: NovaLunaNetworkClient? = null


    /** Current connection state in a network game */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /** Host side: called when createGame() succeeds */
    internal fun onCreateGameResponse(response: CreateGameResponse, hostName: String) {
        // clear any prior data & add the host as first entry
        currentSessionPlayers.clear()
        currentSessionPlayers.add(hostName)
        updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
    }

    /** Guest side: called when joinGame() succeeds */
    internal fun onJoinGameResponse(response: JoinGameResponse, guestName: String) {
        currentSessionPlayers.clear()
        // response.opponents holds host(s)
        currentSessionPlayers.addAll(response.opponents)
        // then ourselves
        currentSessionPlayers.add(guestName)
        updateConnectionState(ConnectionState.WAITING_FOR_INIT)
    }

    /** Host side: called on each new guest join notification */
    internal fun onPlayerJoined(notification: PlayerJoinedNotification) {
        currentSessionPlayers.add(notification.sender)
        onAllRefreshables { refreshAfterPlayerJoined() }
        updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
    }

    internal fun disconnectAndError() {
        currentSessionPlayers.clear()
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Host a new Nova Luna game session on the server.
     * @param secret      Lobby secret/password
     * @param playerName  Display name of the host player
     * @param sessionID   sessionID identifier of the hosted session (to be used by guest on join)
     * @throws IllegalStateException  If already connected or connection fails
     * @throws IllegalArgumentException If secret or name is blank
     */
    fun hostGame(secret: String, playerName: String, sessionID: String? = null) {
        if (!connect(secret, playerName)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, playerName)
        } else {
            client?.createGame(GAME_ID, sessionID, playerName)
        }
        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to server and joins a game session as guest player.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(secret: String, name: String, sessionID: String) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, name)

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }


    /**
     * Establishes a connection to the BGW‑Net server and initializes the client.
     */
    private fun connect(secret: String, playerName: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null) { "Already connected to a game" }
        require(secret.isNotBlank()) { "Server secret must be provided" }
        require(playerName.isNotBlank()) { "Player name must be provided" }

        val newClient = NovaLunaNetworkClient(
            playerName = playerName, host = SERVER_ADDRESS, secret = secret, networkService = this
        )

        return if (newClient.connect()) {
            client = newClient
            true
        } else {
            false
        }
    }


    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }


    /**
     * Starts a hosted Nova Luna game using the exact Player list constructed by your UI.
     *
     * @param playersStartGame Fully initialized Player objects
     *                         (name, tokenCount, colour, type, tiles, height=0)
     * @param isFirstGame      Flag that determines if it's a first game
     * @param randomOrder      From your “random” toggle in the UI
     */
    fun startNewHostedGame(
        playersStartGame: List<Player>, isFirstGame: Boolean, randomOrder: Boolean
    ) {
        // 1) Validate we’re in the lobby
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS) {
            "Not prepared to start a new hosted game (state=$connectionState)"
        }
        require(playersStartGame.size in 2..4) {
            "Player count must be between 2 and 4, but was ${playersStartGame.size}"
        }
        // 2) Annotate onlineMode: local player offline (false), others online (true)
        val annotatedPlayers = playersStartGame.map { p ->
            p.copy( onlineMode = (p.playerName != client?.playerName)
            )
        }


        // 2) Delegate into GameService
        rootService.gameService.startNewGame(annotatedPlayers, 10, randomOrder, false)
        val game = rootService.currentGame ?: error("GameService failed to initialize the game state")

        // 3) Build the InitMessage from the initialized game
        val drawPileIds = game.drawPile.filterNotNull().map { it.id }
        val netPlayers = game.players.map { p ->
            edu.udo.cs.sopra.ntf.subtypes.Player(
                name = p.playerName, color = edu.udo.cs.sopra.ntf.subtypes.Color.valueOf(p.playerColour.name)
            )
        }
        val initMsg = InitMessage(
            drawPile = drawPileIds, isFirstGame = isFirstGame, players = netPlayers
        )

        // Who does the game say is up first?
        val firstIndex = game.activePlayer

        // Which slot in my local list is me?
        val myIndex = game.players.indexOfFirst { it.playerName == client!!.playerName }
        require(myIndex >= 0) { "Local player not found in game.players" }

        // If you’re that slot, it’s your turn, otherwise wait:
        val nextState = if (myIndex == firstIndex) ConnectionState.PLAYING_MY_TURN
        else ConnectionState.WAITING_FOR_OPPONENT

        updateConnectionState(nextState)
        client?.sendGameActionMessage(initMsg) ?: error("Network client not initialized")
        onAllRefreshables { refreshAfterStartGame() }
    }


    /**
     * Called when an InitMessage arrives from the host.
     * Should reconstruct the local game state and transition into WAITING_FOR_OPPONENT.
     */
    fun startNewJoinedGame(message: InitMessage) {
        // 1) Must be waiting for the host’s init
        check(connectionState == ConnectionState.WAITING_FOR_INIT) {
            "Not waiting for init (state=$connectionState)"
        }

        // 2) Compute token counts exactly as in the offline
        val playerCount = message.players.size
        val tokenCount = if (message.isFirstGame) when (playerCount) {
            3 -> 18
            4 -> 16
            else -> 21
        } else 21


        // 3) Rebuild the Player list, annotating onlineMode per slot
        val localPlayers = message.players.map { dto ->
            val isLocal = dto.name == client?.playerName
            Player(
                playerName        = dto.name,
                tokenCount        = tokenCount,
                moonTrackPosition = 0,
                onlineMode        = !isLocal,  // local=false, remote=true
                playerType        = if (isLocal) myPlayerType else PlayerType.HUMAN,
                playerColour      = PlayerColour.valueOf(dto.color.name),
                tiles             = mutableListOf(),
                height            = 0
            )
        }

        // 4) Delegate into game logic
        rootService.gameService.startNewGame(localPlayers, 10, randomOrder = false, firstGame = false)
        val game = rootService.currentGame ?: error("GameService failed to initialize after InitMessage")

        // figure out who starts
        val firstIndex = game.activePlayer
        val myIndex = game.players.indexOfFirst { it.playerName == client!!.playerName }
        require(myIndex >= 0) { "Local player not found in game.players" }

        // transition into the correct state
        val nextState = if (myIndex == firstIndex) ConnectionState.PLAYING_MY_TURN
        else ConnectionState.WAITING_FOR_OPPONENT

        updateConnectionState(nextState)
        onAllRefreshables { refreshAfterStartGame() }

        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        onAllRefreshables { refreshAfterStartGame() }
    }

    /**
     * Send the active‐player’s turn to the opponent.
     *
     * @param tileId       tileId The ID of the tile selected by the player. This corresponds to the ID in the CSV file,
     * @param x            The X-coordinate where the tile is placed.
     * @param y            The Y-coordinate where the tile is placed.
     * @param refillTrack  true indicates that the player has done a refill Action
     */
    fun sendTurnMessage(tileId: Int, x: Int, y: Int, refillTrack: Boolean) {
        // 1) Only send if it’s actually our turn
        check(connectionState == ConnectionState.PLAYING_MY_TURN) {
            "Cannot send turn when not in PLAYING_MY_TURN (state=$connectionState)"
        }

        // 2) Build the TurnMessage as per JSON schema
        val turnMsg = TurnMessage(
            tileId = tileId, x = x, y = y, refillTrack = refillTrack
        )

        // 3) Send the message over BGW-Net
        client?.sendGameActionMessage(turnMsg) ?: error("Network client is not initialized")

        // 4) Switch to waiting for the opponent’s move
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        //rootService.networkService.sendTurnMessage(
        //  tile.id, rowIndex, colIndex, shouldRefillTrack
        //)
        // -->> this to be called whenever you apply a local move (human or bot)
    }

    /**
     * Called when a TurnMessage arrives from the opponent.
     * Applies their move locally and hands control back to us.
     */
    fun receiveTurnMessage(message: TurnMessage) {
        // 1) Only valid when we’re waiting for the opponent
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "Not expecting an opponent move in state=$connectionState"
        }

        // 2) Get the current game
        val game = checkNotNull(rootService.currentGame) {
            "No game in progress when receiving a turn"
        }

        // 3) Locate the tile in the tileTrack by its ID
        val tileIndex = game.tileTrack.indexOfFirst { it?.id == message.tileId }
        require(tileIndex >= 0) {
            "Received TurnMessage for unknown tile ID=${message.tileId}"
        }

        // 4) Apply the move exactly as they did: place the tile at (x,y)
        rootService.playerActionService.playTile(
            tileTrackIndex = tileIndex, position = Coordinate(message.x.toDouble(), message.y.toDouble())
        )

        // 5) If they refilled the wheel as part of their move, do that too
        if (message.refillTrack) {
            rootService.playerActionService.refillWheel()
        }

        //Who’s up next?
        val nextActive = game.activePlayer          // index in game.players
        val myIndex = game.players.indexOfFirst { it.playerName == client!!.playerName }
        require(myIndex >= 0) { "Local player not found in game.players" }

        //Set the correct state
        val nextState = if (myIndex == nextActive) ConnectionState.PLAYING_MY_TURN
        else ConnectionState.WAITING_FOR_OPPONENT

        updateConnectionState(nextState)
    }

    /**
     * Updates the [connectionState] and notifies all registered refreshables.
     */
    fun updateConnectionState(newState: ConnectionState) {
        connectionState = newState
        onAllRefreshables { refreshConnectionState(newState) }
    }

    companion object {
        /** URL of the BGW-Net server for SoPra network games */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Game identifier registered with the server */
        const val GAME_ID = "NovaLuna"
    }
}

