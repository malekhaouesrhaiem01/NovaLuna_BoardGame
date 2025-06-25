package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Initial message sent by the host to the guest to synchronize the game state at the start.
 *
 * @property playerNames List of player names in turn order.
 * @property activePlayerIndex Index of the player who should start the game.
 * @property moonTokenPosition The index of the moon token on the track (0–11).
 * @property tileTrack The list of tile IDs currently on the tile track (may include null at the moon token).
 * @property drawPile The list of remaining tile IDs in the draw pile (top to bottom).
 */
@GameActionClass
data class InitMessage(
    val playerNames: List<String>,
    val activePlayerIndex: Int,
    val moonTokenPosition: Int,
    val tileTrack: List<Int?>,
    val drawPile: List<Int>
) : GameAction() {

    /**
     * String representation for debugging and logging purposes.
     */
    override fun formatMessage(): String {
        return """
            InitMessage(
                playerNames=$playerNames,
                activePlayerIndex=$activePlayerIndex,
                moonTokenPosition=$moonTokenPosition,
                tileTrack=$tileTrack,
                drawPile=${drawPile.take(10)}...
            )
        """.trimIndent()
    }
}
