package entity

/**
 * Represents the full game state of Nova Luna session.
 *
 * @property activePlayer The currently active Player.
 * @property meeplePosition The position of the moon marker in the tile ring.
 * @property beginningOrder The initial turn oder of the players.
 * @property simulationSpeed Speed modifier for the bot?
 * @property players List of players participating in the game.
 * @property drawPile The stack of remaining tiles to be drawn.
 * @property tileTrack The current circual track of visible tiles.
 * @property previousState The previous game state, used for undo function.
 * @property nextState The next game state, used for redo function.
 */
data class NovaLunaGame(var activePlayer: Int,
                        var meeplePosition: Int,
                        var simulationSpeed: Int,
                        val players: MutableList<Player>,
                        val drawPile: MutableList<Tile>,
                        val tileTrack: MutableList<Tile>,
                        var previousState: NovaLunaGame? = null,
                        var nextState: NovaLunaGame? = null)
