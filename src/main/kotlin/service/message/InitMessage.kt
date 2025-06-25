package edu.udo.cs.sopra.ntf.messages

import edu.udo.cs.sopra.ntf.subtypes.Player
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Represents the initial game setup message sent from the host to all guests at the start of a game session.
 *
 * @property drawPile A list of integers representing the tile IDs from the CSV file, randomized by the host.
 * The pile is treated as a stack: [drawPile.first] is the bottom of the stack and [drawPile.last] is the top.
 * Elements are popped from the top to fill the Moon Wheel, starting from the meeple position and continuing clockwise.
 *
 * @property isFirstGame Indicates whether this session is treated as a first game according to the official rules.
 * For example, with 3 [Player]s, each receives 18 tokens; with 4, each gets 16 instead of the standard 21.
 *
 * @property players The list of [Player]s participating in the session. The player at index 0 takes the first turn.
 * Then the list represents the rest of the start order.
 */
@GameActionClass
data class InitMessage(
    val drawPile: List<Int>,
    val isFirstGame: Boolean,
    val players: List<Player>
) : GameAction() {

    /**
     * This method formats the message into a human-readable string.
     * If the return value is `null`, the message will be stringified
     *
     * @return A formatted string representation of the message.
     */
    override fun formatMessage(): String = """
        InitMessage:
        drawPile: $drawPile
        firstGame: $isFirstGame
        players: ${players.joinToString(", ") { it.toString() }}"}
    """.trimIndent()

}