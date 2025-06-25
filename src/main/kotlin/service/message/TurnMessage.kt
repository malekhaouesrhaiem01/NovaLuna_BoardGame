package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Represents a message sent by a player when performing their turn.
 *
 * @property tileId The ID of the tile selected by the player. This corresponds to the ID in the CSV file,
 * similar to how it's used in [InitMessage].
 *
 * @property x The X-coordinate where the tile is placed.
 *
 * @property y The Y-coordinate where the tile is placed.
 *
 * @property refillTrack if true the Boolean indicates that the player has done a refill Action.
 * This should only be sent if there are <=2 Tiles on the MoonWheel.
 * When the MoonWheel is empty and the refill was done automatically by the GameLogic the boolean is also set to True.
 *
 */
@GameActionClass
data class TurnMessage(
    val tileId: Int,
    val x: Int,
    val y: Int,
    val refillTrack: Boolean,
): GameAction() {

    /**
     * This method formats the message into a human-readable string.
     * If the return value is `null`, the message will be stringified
     *
     * @return A formatted string representation of the message.
     */
    override fun formatMessage(): String = """
        TurnMessage:
        - tileID: $tileId
        - x: $x
        - y: $y
        - refillTrack: $refillTrack
    """.trimIndent()

}
