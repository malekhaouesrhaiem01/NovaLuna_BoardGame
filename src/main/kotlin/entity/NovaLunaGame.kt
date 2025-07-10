package entity

import kotlinx.serialization.Serializable

/**
 * Represents the full game state of Nova Luna session.
 *
 * @property activePlayer The currently active Player.
 * @property meeplePosition The position of the moon marker in the tile ring.
 * @property simulationSpeed Speed modifier for the bot?
 * @property players List of players participating in the game.
 * @property drawPile The stack of remaining tiles to be drawn.
 * @property tileTrack The current circular track of visible tiles.
 * @property previousState The previous game state, used for undo function.
 * @property nextState The next game state, used for redo function.
 * @property refilledThisTurn Tracks if a refill action was performed for network messages.
 */
@Serializable
data class NovaLunaGame(var activePlayer: Int,
                        var meeplePosition: Int,
                        var simulationSpeed: Int,
                        val players: MutableList<Player>,
                        val drawPile: MutableList<Tile?>,
                        val tileTrack: MutableList<Tile?>,
                        var firstGame: Boolean,
                        var refilledThisTurn: Boolean = false,
                        var hasPlayedThisTurn: Boolean = false
) {
    /**
     * Creates a deep copy of this game state for use in bot simulations.
     * This method creates new instances of all mutable collections and their contents,
     * ensuring that modifications to the copy don't affect the original game state.
     *
     * @return A deep copy of this game state without any undo/redo chain.
     */
    fun deepCopy(): NovaLunaGame {
        return NovaLunaGame(
            activePlayer = this.activePlayer,
            meeplePosition = this.meeplePosition,
            simulationSpeed = this.simulationSpeed,
            players = this.players.map { it.copy() }.toMutableList(),
            drawPile = this.drawPile.map { it?.copy() }.toMutableList(),
            tileTrack = this.tileTrack.map { it?.copy() }.toMutableList(),
            firstGame = this.firstGame,
            refilledThisTurn = this.refilledThisTurn,
            hasPlayedThisTurn = this.hasPlayedThisTurn
        )
    }
}
