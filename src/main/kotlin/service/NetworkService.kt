package service

import edu.udo.cs.sopra.ntf.messages.InitMessage
import entity.Player
import entity.PlayerColour
import entity.PlayerType

/**
 * Service layer class that realizes the necessary logic for sending and receiving Nova Luna network messages.
 * Bridges between the [NovaLunaNetworkClient] and the other services.
 *
 * @param rootService The [RootService] instance to access game logic and the entity layer
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {

    companion object {
        /** URL of the BGW-Net server for SoPra network games */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Game identifier registered with the server */
        const val GAME_ID = "NovaLuna"
    }

    /** Underlying BGW-Net client; null when offline */
    private var client: NovaLunaNetworkClient? = null

    /** Current connection state in a network game */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

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
     * Establishes a connection to the BGW‑Net server and initializes the client.
     */
    private fun connect(secret: String, playerName: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null) { "Already connected to a game" }
        require(secret.isNotBlank()) { "Server secret must be provided" }
        require(playerName.isNotBlank()) { "Player name must be provided" }

        val newClient = NovaLunaNetworkClient(
            playerName = playerName,
            host = SERVER_ADDRESS,
            secret = secret,
            networkService = this
        )

        return if (newClient.connect()) {
            client = newClient
            true
        } else {
            false
        }
    }

    /**
     * Starts the hosted game: initializes the local game and sends the initial state to all connected players.
     * @param playerNames List of player names in turn order.
     */
    fun startNewHostedGame(playerNames: List<String>) {
        // Ensure correct state
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS) { "Not prepared to start a new hosted game." }

        // Create Player objects from player names
        // For network games, you might want to store player types separately
        // or determine them based on your lobby setup
        val players = playerNames.mapIndexed { index, name ->
            Player(
                playerName = name,
                tokenCount = 21, // Standard token count - will be adjusted based on isFirstGame  <-----------------------------------------------------------------------------------------------
                moonTrackPosition = 0,
                onlineMode = true, // This is a network game
                playerType = PlayerType.HUMAN, // Default to human for network games
                // Add logic to see if its a bot or not <-----------------------------------------------------------------------------------------------
                playerColour = when(index) {
                    0 -> PlayerColour.BLUE
                    1 -> PlayerColour.ORANGE
                    2 -> PlayerColour.BLACK
                    3 -> PlayerColour.WHITE
                    else -> PlayerColour.BLUE // fallback, though shouldn't be needed
                },
                tiles = mutableListOf(),
                height = 4 - index // Starting heights: player 0 gets 4, player 1 gets 3, etc.
            )
        }

        // Initialize the game locally using Player objects
        rootService.gameService.startNewGame(players, 10)
        val game = rootService.currentGame
        checkNotNull(game) { "Game should be initialized before sending init message." }

        // Serialize the draw pile by CSV IDs
        val drawIds = game.drawPile.filterNotNull().map { it.id }

        // Map domain players to network with name and color
        val netPlayers = game.players.map { player ->
            val netColor = edu.udo.cs.sopra.ntf.subtypes.Color.valueOf(player.playerColour.name)
            edu.udo.cs.sopra.ntf.subtypes.Player(
                name = player.playerName,
                color = netColor
            )
        }

        // Determine if this is a first game
        val isFirstGame = false // false as default - wheres the flag?  <-----------------------------------------------------------------------------------------------

        // Adjust token counts based on game variant
        if (isFirstGame) {
            players.forEach { player ->
                player.tokenCount = when (players.size) {
                    3 -> 18
                    4 -> 16
                    else -> 21 // fallback to standard
                }
            }
        }

        // Build and send the InitMessage
        val initMsg = InitMessage(
            drawPile = drawIds,
            isFirstGame = isFirstGame,
            players = netPlayers
        )

        updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        client?.sendGameActionMessage(initMsg)
    }

    /**
     * Updates the [connectionState] and notifies all registered refreshables.
     */
    private fun updateConnectionState(newState: ConnectionState) {
        connectionState = newState
        onAllRefreshables { refreshConnectionState(newState) }
    }
    }

