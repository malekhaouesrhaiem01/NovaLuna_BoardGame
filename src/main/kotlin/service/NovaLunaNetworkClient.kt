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
import tools.aqua.bgw.core.BoardGameApplication

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

    fun isAnnotatedReceiversReady(): Boolean = annotatedReceiversReady
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
            "Unexpected CreateGameResponse"
        }

        if (response.status == CreateGameResponseStatus.SUCCESS) {
            sessionID = response.sessionID
            // forward into the service (which itself updates the state)
            networkService.onCreateGameResponse(response, playerName)
        } else {
            disconnectAndError("CreateGame failed: ${response.status}")
        }
    }

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

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        // FIX: Allow both WAITING_FOR_GUESTS and WAITING_FOR_INIT states
        check(
            networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_INIT
        ) { "Not awaiting any guests or init." }

        otherPlayerName = notification.sender
        networkService.onPlayerJoined(notification )
    }

    override fun onGameActionResponse(response: GameActionResponse) {
        check(
            networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT
        ) { "Not in a playable network state." }

        if (response.status != GameActionResponseStatus.SUCCESS) {
            disconnectAndError("GameAction failed: ${response.status}")
        }
    }


    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onInitReceived(message: InitMessage, sender: String) {
        println("DEBUG: onInitReceived called with sender: $sender")
        println("DEBUG: Current connection state: ${networkService.connectionState}")

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
        } catch (e: Exception) {
            println("DEBUG: Exception in startNewJoinedGame: ${e.message}")
            e.printStackTrace()
        }
    }

    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onTurnReceived(message: TurnMessage, sender: String) {

            networkService.receiveTurnMessage(message)
          }

    private fun disconnectAndError(reason: Any) {
        networkService.disconnect()
        error(reason)
    }
}