package service

import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage

/**
 * This class handles all networking logic for the Nova Luna game by extending [BoardGameClient].
 * It receives and processes game-related messages from the server and delegates actions to [NetworkService].
 *
 * @param playerName The name of the local player.
 * @param host The hostname or IP of the server to connect to.
 * @param secret A shared secret for authentication.
 * @param networkService The associated [NetworkService] handling game logic integration.
 */
class NovaLunaNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    private val networkService: NetworkService
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    var sessionID: String? = null
    var otherPlayerName: String? = null



    // Add this flag to track if we're ready to receive messages
    @Volatile
    private var annotatedReceiversReady = false

    // Override this method to detect when receivers are ready
    override fun onOpen() {
        super.onOpen()
        // Start a background thread to wait for receiver initialization
        Thread {
            Thread.sleep(2000) // Give framework time to initialize
            annotatedReceiversReady = true
            println("DEBUG: Annotated receivers are now ready")
        }.start()
    }

    /**
     * Handles the server response after trying to create a game.
     * If successful, stores the session ID and informs the [NetworkService].
     * Otherwise, disconnects and throws an error.
     *
     * @param response The server's response to a create game request.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
            "Unexpected CreateGameResponse"
        }

        if (response.status == CreateGameResponseStatus.SUCCESS) {
            val id = response.sessionID
                ?: throw IllegalArgumentException("SessionID must not be null when status is SUCCESS.")
            sessionID = id
            networkService.onCreateGameResponse(response, playerName)
        } else {
            disconnectAndError("CreateGame failed: ${response.status}")
        }
    }
    /**
     * Handles the server response after trying to join a game.
     * On success, sets session ID and opponent name, and informs the [NetworkService].
     * On failure, disconnects and throws an error.
     *
     * @param response The server's response to a join game request.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION) {
            "Unexpected JoinGameResponse"
        }

        if (response.status == JoinGameResponseStatus.SUCCESS) {
            sessionID = response.sessionID
            otherPlayerName = response.opponents.firstOrNull()
            // forward into the service (which itself updates the state)
            networkService.onJoinGameResponse(response, playerName)
        } else {
            disconnectAndError("JoinGame failed: ${response.status}")
        }
    }
    /**
     * Called when another player joins the game lobby.
     * Updates the [otherPlayerName] and forwards the notification to the [NetworkService].
     *
     * @param notification The join notification sent by the server.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        // FIX: Allow both WAITING_FOR_GUESTS and WAITING_FOR_INIT states
        check(
            networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_INIT
        ) { "Not awaiting any guests or init." }

        otherPlayerName = notification.sender
        networkService.onPlayerJoined(notification )
    }
    /**
     * Handles a game action response from the server.
     * If the action was not successful, disconnects and throws an error.
     *
     * @param response The response to a previous game action.
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        check(
            networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT
        ) { "Not in a playable network state." }

        if (response.status != GameActionResponseStatus.SUCCESS) {
            disconnectAndError("GameAction failed: ${response.status}")
        }
    }

    /**
     * Receiver for the initial game state message from the host.
     * Delegates handling to the [NetworkService].
     *
     * @param message The initial game setup message.
     * @param sender The sender of the message.
     */
    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onInitReceived(message: InitMessage, sender: String) {
        println("DEBUG: onInitReceived called with sender: $sender")
        println("DEBUG: Current connection state: ${networkService.connectionState}")

        if (sender == this.playerName) {
            println("DEBUG: Ignoring my own InitMessage")
            return
        }

        // Wait for receivers to be ready if needed
        if (!annotatedReceiversReady) {
            println("DEBUG: Waiting for annotated receivers to be ready...")
            var waited = 0
            while (!annotatedReceiversReady && waited < 50) { // 5 seconds max
                Thread.sleep(100)
                waited++
            }
            if (annotatedReceiversReady) {
                println("DEBUG: Annotated receivers now ready after ${waited * 100}ms")
            } else {
                println("DEBUG: WARNING: Annotated receivers still not ready after timeout")
            }
        }

        try {
            networkService.startNewJoinedGame(message)
            println("DEBUG: startNewJoinedGame completed successfully")
        } catch (e: IllegalStateException) {
            println("DEBUG: Exception in startNewJoinedGame: ${e.message}")
            e.printStackTrace()
        }
    }
    /**
     * Receiver for turn update messages from the host.
     * Delegates the message to the [NetworkService].
     *
     * @param message The turn message sent by the host.
     * @param sender The sender (host) of the message.
     */
    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onTurnReceived(message: TurnMessage, sender: String) {
        // ignore our own messages (the server echoes them back to us)
        if (sender == this.playerName) {
            println("DEBUG: Ignoring my own TurnMessage for tile ${message.tileId}")
            return
        }
            networkService.receiveTurnMessage(message, sender)
          }

    private fun disconnectAndError(reason: Any) {
        networkService.disconnect()
        error(reason)
    }
}