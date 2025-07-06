package entity

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
data class NovaLunaGame(var activePlayer: Int,
                        var meeplePosition: Int,
                        var simulationSpeed: Int,
                        val players: MutableList<Player>,
                        val drawPile: MutableList<Tile?>,
                        val tileTrack: MutableList<Tile?>,
                        var previousState: NovaLunaGame? = null,
                        var nextState: NovaLunaGame? = null,
                        var firstGame: Boolean
                        var refilledThisTurn: Boolean = false
) : Cloneable{
    public override fun clone(): NovaLunaGame {
        val copiedPlayers = players.map{ it.clone() }.toMutableList()
        val copiedDrawPile = drawPile.map { it?.copy() }.toMutableList()
        val copiedTileTrack = tileTrack.map { it?.copy() }.toMutableList()

        return NovaLunaGame(this.activePlayer, this.meeplePosition, this.simulationSpeed,
            copiedPlayers, copiedDrawPile, copiedTileTrack, previousState, null, firstGame = this.firstGame)
    }
}
