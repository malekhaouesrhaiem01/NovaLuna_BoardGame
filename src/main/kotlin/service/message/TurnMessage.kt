package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Message representing a single player turn in Nova Luna.
 * Sent from the active player to the opponent via the network.
 *
 * @property tileId The unique ID of the tile the player picked.
 * @property x The x-coordinate where the tile was placed.
 * @property y The y-coordinate where the tile was placed.
 * @property refillTrack Whether the tile track was refilled after the move.
 */
@GameActionClass
data class TurnMessage(
    val tileId: Int,
    val x: Int,
    val y: Int,
    val refillTrack: Boolean
) : GameAction() {

    override fun formatMessage(): String {
        return "TurnMessage(tileId=$tileId, x=$x, y=$y, refillTrack=$refillTrack)"
    }
}
// noch nicht fertig