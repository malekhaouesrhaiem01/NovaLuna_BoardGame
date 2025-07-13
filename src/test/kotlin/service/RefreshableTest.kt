package service

import kotlin.test.Test
import entity.Player
import entity.PlayerType
import entity.PlayerColour

class RefreshableTest {

    @Test
    fun defaultMethodsAreNoOp() {
        val ref = object : Refreshable {}

        // No exception should be thrown by any of these calls:
        ref.refreshAfterStartTurn()
        ref.refreshAfterEndTurn()
        ref.refreshAfterStartGame()
        ref.refreshAfterTilePlayed()
        ref.refreshAfterUndo()
        ref.refreshAfterRedo()
        ref.refreshAfterRefill()
        ref.refreshAfterRageQuit()
        ref.refreshAfterPlayerJoined()

        // refreshAfterGameEnd takes a Player parameter
        val dummy = Player(
            playerName        = "Test",
            tokenCount        = 0,
            moonTrackPosition = 0,
            onlineMode        = false,
            playerType        = PlayerType.HUMAN,
            playerColour      = PlayerColour.WHITE,
            tiles             = mutableListOf(),
            height            = 0
        )
        ref.refreshAfterGameEnd(dummy)

        // refreshConnectionState takes a ConnectionState
        ConnectionState.entries.forEach { state ->
            ref.refreshConnectionState(state)
        }
    }
}