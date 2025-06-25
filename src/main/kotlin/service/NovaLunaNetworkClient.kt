package service

import service.message.InitMessage
import service.message.TurnMessage
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

/**
 * Network client for Nova Luna using the BoardGameWork framework.
 *
 * This client handles all incoming messages from the BGW server
 * and forwards them to the [NovaLunaNetworkService] for further processing.
 *
 * @param playerName The name of the local player.
 * @param host The server host address to connect to.
 * @param secret The game session secret (used to join/create games).
 * @param networkService The service that handles connection and game state logic.
 */
class NovaLunaNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    private val networkService: NovaLunaNetworkService
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    /** The current game session ID, assigned by the server. */
    var sessionID: String? = null

    /** The name of the opponent, received via the join response or player join notification. */
    var otherPlayerName: String? = null

    /**
     * Called when a [CreateGameResponse] is received after the host requested to start a game.
     * Validates the connection state, stores the session ID, and transitions to waiting for guest.
     *
     * @param response The response object containing session ID and status.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
            "Unexpected CreateGameResponse"
        }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                sessionID = response.sessionID
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Called when a [JoinGameResponse] is received after a guest attempts to join a session.
     * Stores opponent name and session ID, then waits for the host's init message.
     *
     * @param response The server's response to the join request.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION) {
            "Unexpected JoinGameResponse"
        }

        when (response.status) {
            JoinGameResponseStatus.SUCCESS -> {
                otherPlayerName = response.opponents.firstOrNull()
                sessionID = response.sessionID
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Called when a [PlayerJoinedNotification] is received.
     * This occurs when a guest successfully joins a hosted game.
     * Passes control to the network service to start the game and send an [InitMessage].
     *
     * @param notification The notification containing the guest's name.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS) {
            "Not awaiting any guests."
        }

        otherPlayerName = notification.sender
        networkService.startNewHostedGame(playerName, notification.sender)
    }

    /**
     * Called when the server responds to a previously sent action (e.g. a turn).
     * If successful, no action is needed. Otherwise, the connection is closed with an error.
     *
     * @param response The response to a game action (e.g. move).
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        check(
            networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT
        ) { "Not in a playable network state." }

        when (response.status) {
            GameActionResponseStatus.SUCCESS -> {
                // nothing to do
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Called when the [InitMessage] is received from the host.
     * Passes the message to the network service to initialize the local game state.
     *
     * @param message The initial game state sent by the host.
     * @param sender The name of the sender (host).
     */
    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onInitReceived(message: InitMessage, sender: String) {
        networkService.startNewJoinedGame(message)
    }

    /**
     * Called when a [TurnMessage] is received from the opponent.
     * Passes the move to the network service to apply it.
     *
     * @param message The turn information sent by the other player.
     * @param sender The name of the opponent who sent the move.
     */
    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onTurnReceived(message: TurnMessage, sender: String) {
        networkService.receiveTurnMessage(message)
    }

    /**
     * Disconnects from the server and throws an error with the given message.
     *
     * @param reason The reason for disconnecting or erroring.
     */
    private fun disconnectAndError(reason: Any) {
        networkService.disconnect()
        error(reason)
    }
}
