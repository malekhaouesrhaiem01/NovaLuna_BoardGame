package service

import service.message.InitMessage
import service.message.TurnMessage

/**
 * Service layer that handles multiplayer logic for Nova Luna.
 * Bridges the [NovaLunaNetworkClient] and the game services.
 *
 * @param rootService Provides access to all game services and entities.
 */
class NovaLunaNetworkService(
    private val rootService: RootService
) : AbstractRefreshingService() {

    companion object {
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"
        const val GAME_ID = "NovaLuna"
    }

    /** Network client. Null when offline. */
    var client: NovaLunaNetworkClient? = null
        private set

    /** Current network connection state */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    // Called by host to create game
    fun hostGame(secret: String, name: String, sessionID: String?) {
        // TODO: implement later
    }

    // Called by guest to join game
    fun joinGame(secret: String, name: String, sessionID: String?) {
        // TODO: implement later
    }

    // Called after CreateGameResponse or JoinGameResponse
    private fun connect(secret: String, name: String): Boolean {
        // TODO: implement later
        return false
    }

    // Cleanly disconnect
    fun disconnect() {
        // TODO: implement later
    }

    // Called when guest joins; sends InitMessage to them
    fun startNewHostedGame(hostPlayerName: String, guestPlayerName: String) {
        // TODO: implement later
    }

    // Called when InitMessage is received from host
    fun startNewJoinedGame(message: InitMessage) {
        // TODO: implement later
    }

    // Host or guest sends TurnMessage after making a move
    fun sendTurnMessage(tileId: Int, x: Int, y: Int, refillTrack: Boolean) {
        // TODO: implement later
    }

    // Called when opponent's TurnMessage is received
    fun receiveTurnMessage( message: TurnMessage) {
        // TODO: implement later
    }

    // Connection state update + notify GUI
    fun updateConnectionState(newState: ConnectionState) {
        // TODO: implement later
    }
}
