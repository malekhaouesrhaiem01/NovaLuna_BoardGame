package service


import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.SerializableCoordinate


/**
 * Service layer class that realizes the necessary logic for sending and receiving Nova Luna network messages.
 * Bridges between the [NovaLunaNetworkClient] and the other services.
 *
 * @param rootService The [RootService] instance to access game logic and the entity layer
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {


    /** What *this* client chose — HUMAN, EASYBOT, or HARDBOT */
    var myPlayerType: PlayerType = PlayerType.HUMAN

    /** Tracks host + guest names in join order */
    val currentSessionPlayers = mutableListOf<String>()

    private val playerObjectIds = mutableMapOf<String, Int>()

    /** Underlying BGW-Net client; null when offline */
    private var client: NovaLunaNetworkClient? = null


    /** Current connection state in a network game */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Publicly expose the sessionID of the current lobby (set on create or join).
     */
    var currentSessionID: String? = null
        private set


    /** Host side: called when createGame() succeeds */
    internal fun onCreateGameResponse(response: CreateGameResponse, hostName: String) {
        // clear any prior data & add the host as first entry
        currentSessionPlayers.clear()
        currentSessionPlayers.add(hostName)
        currentSessionID = response.sessionID
        updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
    }

    /** Guest side: called when joinGame() succeeds */
    internal fun onJoinGameResponse(response: JoinGameResponse, guestName: String) {
        currentSessionPlayers.clear()
        // response.opponents holds host(s)
        currentSessionPlayers.addAll(response.opponents)
        // then ourselves
        currentSessionPlayers.add(guestName)
        currentSessionID = response.sessionID
        updateConnectionState(ConnectionState.WAITING_FOR_INIT)
        onAllRefreshables { refreshAfterPlayerJoined() }

    }

    /** Host side: called on each new guest join notification */
    internal fun onPlayerJoined(notification: PlayerJoinedNotification) {
        currentSessionPlayers.add(notification.sender)
        onAllRefreshables { refreshAfterPlayerJoined() }
    }

   // internal fun disconnectAndError() {
     //   currentSessionPlayers.clear()
     //   updateConnectionState(ConnectionState.DISCONNECTED)
   // }

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
        currentSessionID = null // Clear session ID on disconnect
        currentSessionPlayers.clear() // Clear player list on disconnect
        updateConnectionState(ConnectionState.DISCONNECTED)
    }


    /**
     * Starts a hosted Nova Luna game using the exact Player list constructed by your UI.
     *
     * @param playersStartGame Fully initialized Player objects
     *                         (name, tokenCount, colour, type, tiles, height=0)
     * @param isFirstGame      Flag that determines if it's a first game
     * @param randomOrder      From "random" toggle in the UI
     */
    fun startNewHostedGame(
        playersStartGame: List<Player>, isFirstGame: Boolean, randomOrder: Boolean
    ) {
        // 1) Validate we're in the lobby
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS) {
            "Not prepared to start a new hosted game (state=$connectionState)"
        }
        require(playersStartGame.size in 2..4) {
            "Player count must be between 2 and 4, but was ${playersStartGame.size}"
        }

        // 2) Annotate onlineMode: local player offline (false), others online (true)
        val annotatedPlayers = playersStartGame.map { p ->
            p.copy(onlineMode = (p.playerName != client?.playerName))
        }

        // 3) Delegate into GameService (host still uses regular startNewGame to shuffle)
        rootService.gameService.startNewGame(
            annotatedPlayers,
            10,
            randomOrder,
            isFirstGame,
            startTurnImmediately = false)
        val game = rootService.currentGame ?: error("GameService failed to initialize the game state")
        game.players.forEach {
            playerObjectIds[it.playerName] = System.identityHashCode(it)
        }
        //println("   [HOST] Player object IDs: $playerObjectIds")

        // 4) Build and send the InitMessage using the ACTUAL shuffled tile order
        val drawPileIds = mutableListOf<Int>()

        // Get the actual tile order from the game (tileTrack + drawPile)
        // Skip index 0 of tileTrack (it's null for meeple)
        for (i in 1 until game.tileTrack.size) {
            game.tileTrack[i]?.let { drawPileIds.add(it.id) }
        }

        // Add all tiles from drawPile
        game.drawPile.forEach { tile ->
            tile?.let { drawPileIds.add(it.id) }
        }
        val netPlayers = game.players.map { p ->
            edu.udo.cs.sopra.ntf.subtypes.Player(
                name = p.playerName, color = edu.udo.cs.sopra.ntf.subtypes.Color.valueOf(p.playerColour.name)
            )
        }

        drawPileIds.reverse()
        val initMsg = InitMessage(
            drawPile = drawPileIds, isFirstGame = isFirstGame, players = netPlayers
        )

        println("INIT-MSG ➜ $initMsg")

        // 5) Send the InitMessage first, before determining states
        client?.sendGameActionMessage(initMsg) ?: error("Network client not initialized")

        // 6) Who does the game say is up first?
        val firstIndex = game.activePlayer

        // 7) Which slot in my local list is me?

        val myIndex = game.players.indexOfFirst { it.playerName == client?.playerName }
        require(myIndex >= 0) { "Local player not found in game.players" }

        // 8) If you're that slot, it's your turn, otherwise wait:
        val nextState = if (myIndex == firstIndex) ConnectionState.PLAYING_MY_TURN
        else ConnectionState.WAITING_FOR_OPPONENT

        updateConnectionState(nextState)

        //start turn here
        rootService.gameService.startTurn()

        onAllRefreshables { refreshAfterStartGame() }
    }


    /**
     * Called when an InitMessage arrives from the host.
     * Should reconstruct the local game state and transition into WAITING_FOR_OPPONENT.
     */
    fun startNewJoinedGame(message: InitMessage) {

        println("DEBUG: startNewJoinedGame called, current state: $connectionState")

        // 1) Must be waiting for the host's init
        check(connectionState == ConnectionState.WAITING_FOR_INIT) {
            "Not waiting for init (state=$connectionState)"
        }

        println("DEBUG: State check passed, building players...")

        // 2) Compute token counts exactly as in the offline
        val playerCount = message.players.size
        val tokenCount = if (message.isFirstGame) when (playerCount) {
            3 -> 18 - 1
            4 -> 16 - 1
            else -> 21 - 1
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
        rootService.gameService.startNetworkGame(
            localPlayers,
            10,
            message.drawPile,
            message.isFirstGame,
            startTurnImmediately = false)

        val game = rootService.currentGame ?: error("GameService failed to initialize after InitMessage")
        game.players.forEach {
            playerObjectIds[it.playerName] = System.identityHashCode(it)
        }
        //println("   [GUEST] Player object IDs: $playerObjectIds")
        // 5) Figure out who starts
        val firstIndex = game.activePlayer
        val myIndex = game.players.indexOfFirst { it.playerName == client?.playerName }
        require(myIndex >= 0) { "Local player not found in game.players" }

        // 6) Transition into the correct state
        val nextState = if (myIndex == firstIndex) ConnectionState.PLAYING_MY_TURN
        else ConnectionState.WAITING_FOR_OPPONENT
        println("DEBUG: About to transition to state: $nextState")

        updateConnectionState(nextState)
        println("DEBUG: State transition completed, new state: $connectionState")

        //start turn here
        rootService.gameService.startTurn()

        onAllRefreshables { refreshAfterStartGame() }
    }

    /**
     * Send the active‐player's turn to the opponent.
     *
     * @param tileId       The ID of the tile selected by the player. This corresponds to the ID in the CSV file,
     * @param x            The X-coordinate where the tile is placed.
     * @param y            The Y-coordinate where the tile is placed.
     * @param refillTrack  true indicates that the player has done a refill Action
     */
    fun sendTurnMessage(tileId: Int, x: Int, y: Int, refillTrack: Boolean) {


        println("   SEND TURN MESSAGE:")
        println("   - Tile ID: $tileId")
        println("   - Position: ($x, $y)")
        println("   - Refill included: $refillTrack")
        println("   - Current state: $connectionState")

        // 1) Only send if it's actually our turn
        check(connectionState == ConnectionState.PLAYING_MY_TURN) {
            "Cannot send turn when not in PLAYING_MY_TURN (state=$connectionState)"
        }

        // 2) Build the TurnMessage as per JSON schema
        val turnMsg = TurnMessage(
            tileId = tileId, x = x, y = y, refillTrack = refillTrack
        )

        // 3) Send the message over BGW-Net
        client?.sendGameActionMessage(turnMsg) ?: error("Network client is not initialized")

        println("   - Changing state to WAITING_FOR_OPPONENT")

        // 4) Switch to waiting for the opponent's move
        updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        println("   - Calling endTurn()")

        rootService.gameService.endTurn()
        println("    Turn ended")

        val game = rootService.currentGame
        if (game != null) {
            val myIndex = game.players.indexOfFirst { it.playerName == client?.playerName }

            if (myIndex == game.activePlayer) {
                // We get another turn!
                println("DEBUG: Same player gets another turn")
                updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            }
            //println("[SEND] ${client!!.playerName} thinks next player is:
            //${game.players[game.activePlayer].playerName}")
            //println("[SEND] ${client!!.playerName} connection state is now: $connectionState")
        } else {
            // Game ended, update connection state appropriately
            updateConnectionState(ConnectionState.DISCONNECTED)
            println("[SEND] Game ended during turn processing")
        }
    }

    /**
     * Called when a TurnMessage arrives from the opponent.
     * Applies their move locally and hands control back to us.
     */
    fun receiveTurnMessage(message: TurnMessage, sender: String) {

        

        println("  RECEIVE TURN MESSAGE:")
        println("   - From player: $sender")
        println("   - Tile ID: ${message.tileId}")
        println("   - Refill included: ${message.refillTrack}")
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "Not expecting an opponent move in state=$connectionState"
        }

        val game = checkNotNull(rootService.currentGame) {
            "No game in progress when receiving a turn"
        }
        val senderIndex = game.players.indexOfFirst { it.playerName == sender }
        require(senderIndex >= 0) { "Unknown sender: $sender" }
        game.activePlayer = senderIndex
      

        // Apply refill if needed
        if (message.refillTrack) {
            rootService.playerActionService.refillWheel()
        }

        // Place the tile exactly as done by the opponent
        val tileIndex = game.tileTrack.indexOfFirst { it?.id == message.tileId }
        require(tileIndex >= 0) { "Unknown tile ID=${message.tileId}" }




        rootService.playerActionService.playTile(
            tileTrackIndex = tileIndex,
            position = SerializableCoordinate(message.x.toDouble(), message.y.toDouble())
        )



        // IMPORTANT: Check if game still exists after playTile.
        // PlayTile might have ended the game, so we need to check before calling endTurn
        val currentGame = rootService.currentGame
        if (currentGame != null) {
            // End their turn
            rootService.gameService.endTurn()

            // Check again if game still exists after endTurn
            val gameAfterEndTurn = rootService.currentGame
            if (gameAfterEndTurn != null) {
                // Decide next connection state: if the same player remains active, grant another turn
                val nextState =
                    if (gameAfterEndTurn.players[gameAfterEndTurn.activePlayer].playerName == client?.playerName) {
                        ConnectionState.PLAYING_MY_TURN
                    } else {
                        ConnectionState.WAITING_FOR_OPPONENT
                    }

                updateConnectionState(nextState)
               // println("Who does ${client!!.playerName} think is next:
                // ${gameAfterEndTurn.players[gameAfterEndTurn.activePlayer].playerName}")
                //println("My connection state will be: $nextState")
               // println("DEBUG: Transitioning to connectionState=$nextState for player {client.playerName}")
            } else {
                // Game ended, disconnect or go to appropriate state
                updateConnectionState(ConnectionState.DISCONNECTED)
                println("Game ended during network turn processing")
            }
        } else {
            // Game ended during playTile, handle appropriately
            updateConnectionState(ConnectionState.DISCONNECTED)
            println("Game ended during opponent's move")
        }
    }

    /**
     * Updates the [connectionState] and notifies all registered refreshables.
     */
    fun updateConnectionState(newState: ConnectionState) {
        connectionState = newState
        onAllRefreshables { refreshConnectionState(newState) }
    }

    /**
     * Holds all constants required to connect to the BGW-Net server
     * for SoPra network games.
     */
    companion object {
        /** URL of the BGW-Net server for SoPra network games */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Game identifier registered with the server */
        const val GAME_ID = "NovaLuna"
    }
}