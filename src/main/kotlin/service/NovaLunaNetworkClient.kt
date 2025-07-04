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
            requireNotNull(response.sessionID) { "sessionID must not be null on SUCCESS" }
            sessionID = response.sessionID
            // forward into the service (which itself updates the state)
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
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS) {
            "Not awaiting any guests."
        }

        otherPlayerName = notification.sender
        // only update the lobby list , not auto start
        networkService.onPlayerJoined(notification)
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
     * @param sender The sender (host) of the message.
     */
    @GameActionReceiver
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onInitReceived(message: InitMessage, sender: String) {
        networkService.startNewJoinedGame(message)
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
        networkService.receiveTurnMessage(message)
    }
    /**
     * Disconnects the client and throws an error with the given reason.
     *
     * @param reason The reason for disconnection.
     */
    private fun disconnectAndError(reason: Any) {
        networkService.disconnect()
        error(reason)
    }
}