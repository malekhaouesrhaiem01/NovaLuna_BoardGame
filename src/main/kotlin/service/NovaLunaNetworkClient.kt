package service

import service.message.InitMessage
import service.message.TurnMessage
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

class NovaLunaNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    private val networkService: NovaLunaNetworkService
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    var sessionID: String? = null
    var otherPlayerName: String? = null

    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
            "Unexpected CreateGameResponse"
        }

        if (response.status == CreateGameResponseStatus.SUCCESS) {
            sessionID = response.sessionID
            networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
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
            networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
        } else {
            disconnectAndError("JoinGame failed: ${response.status}")
        }
    }

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS) {
            "Not awaiting any guests."
        }

        otherPlayerName = notification.sender
        networkService.startNewHostedGame(playerName, notification.sender)
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
        networkService.startNewJoinedGame(message)
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
