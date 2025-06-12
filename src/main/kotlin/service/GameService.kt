package service

import entity.NovaLunaGame
import entity.Player
import entity.Move
import entity.Tile
import tools.aqua.bgw.util.Coordinate

class GameService(private val rootService: RootService) : AbstractRefreshingService() {
    /**
     * Starts a new Nova Luna game with the given players and simulation speed.
     * the game state + drawpile + tile track are initialized.
     * Triggers [refreshAfterGameStarted] to update the UI with the initial game state.
     * @param players The list of players participating in the game (2 to 4 players).
     * @param simulationSpeed The speed of the game simulation (0 to 10)
     * @throws IllegalStateException If a game is already running.
     * @throws IllegalArgumentException or if the simulation speed is greater than 10.
     * @throws IllegalArgumentException If the number of players is not between 2 and 4
     */
    fun startNewGame(players : List<Player>, simulationSpeed : Int) {

        // überprüfe, ob Anzahl der Spieler passt (2 bis 4)
        require(players.size in 2..4) { "Spieleranzahl muss zwischen 2 und 4 sein." }

        require(simulationSpeed < 11) { "SimulationSpeed darf maximal 10 sein" }
        // Überprüfe, ob bereits ein Spiel läuft
        if (rootService.currentGame != null) {
            throw IllegalStateException("A game is already running.")
        }
        // Initializing the draw pile
        val drawPile = rootService.tileLoader.readTiles().shuffled().toMutableList()

        // Initializing the tileTrack with the top 11 tiles from the drawPile
        val tileTrack = drawPile.subList(0, 11)
        drawPile.subList(0, 11).clear()

        // setting heights according to beginning order
        players[0].height <- 4
        players[1].height <- 3
        players[2].height <- 2
        players[3].height <- 1

        val game = NovaLunaGame(
            activePlayer = 0,
            meeplePosition = 0,
            simulationSpeed = simulationSpeed,
            players = players.toMutableList(),
            drawPile = drawPile,
            tileTrack = tileTrack
        )

        rootService.currentGame = game

    }

    /**
     * Checks if one of the two conditions for ending the game are fulfilled.
     * 1. A player has placed all his tokens
     * 2. All tiles are laid out so no tile can be played anymore
     *
     * Precondition:
     * - A game has to be running
     *
     * Postcondition:
     * - When the method returns true, endGame() has to be activated
     *
     * @returns This method returns a boolean
     *
     * @throws IllegalStateException If no game is currently running
     */
    fun checkEndGame() : Boolean {
        if (rootService.currentGame == null) {
            throw IllegalStateException("No game is currently running.")
        }

        val game = rootService.currentGame!!

        if(game.players[game.activePlayer].tokenCount < 1){
            return true
        }
        else if(game.tileTrack.size == 0 && game.drawPile.size == 0){
            return true
        }

        return false
    }

    /**
     * The method [startTurn] starts the turn for the current Player
     * and calls the refreshable to change in scenery in the GUI
     *
     * Preconditions:
     * - Game must be started
     * - It has to be a Players turn
     *
     * Postconditions:
     * - Aktueller Spieler ist am Zug
     * - Punkte des Aktuellen Spielers werden angezeigt
     * - Game Matrix des Aktuellen Spielers wird angezeigt
     * - Ansicht wird auf den des Aktuellen Spielers geändert
     *
     * Exceptions:
     * @throws IllegalStateException is thrown, when no game exists.
     */

    fun startTurn(){
        val game = rootService.currentGame
        checkNotNull(game)

        onAllRefreshables { refreshAfterStartTurn() }
    }

    /**
     * The method [endTurn] ends the current Players turn,
     * as well as changing the current Player to the Player next in line.
     * Calls up the refreshables to update the GUI Scenery.
     * Unit beendet den Zug des aktuellen Spielers. Dabei wird auch der nächste Spieler der am Zug ist als aktuellen Spieler gesetzt. Ebenfalls werden refreshables für die GUI aufgerufen um den ConfirmNextPlayerScene aufzurufen.
     *
     * Preconditions:
     * - Game must be started
     * - It has to be a Players Turn
     * - Player has picked and Played his card
     *
     * Postconditions:
     * - ConfirmNextPlayerScene will be displayed
     * - The current Player will be changed to the next Player in line
     *
     * Exceptions:
     * @throws IllegalStateException is thrown, when no Game exists
     */
    fun endTurn(){
    }

    /**
    * Ends the current Nova Luna game by determining the winner,
    * displaying player scores, and clearing the game state.
    *
    * This method is  called from [checkEndGame] when an end condition is met.
    * Triggers [refreshAfterGameEnd] to update the UI with the winner name and players  scores  .
    * @throws IllegalStateException if no game is currently active or the game is already ended
    */
    fun endGame(){}


    /**
     * Checks whether there are fewer than three tiles on the `tileTrack`
     * and refills it with new tiles from the `drawPile` if necessary.
     *
     * Preconditions:
     * - A running game (`currentGame`) must exist.
     *
     * Postconditions:
     * - The `tileTrack` contains at least three tiles, provided enough tiles are available in the `drawPile`.
     * - The appropriate refresh methods are called to update the GUI.
     *
     * @throws IllegalStateException If no game is currently active.
     *
     * @return This method has no return value.
     *
     * @sample checkRefill()
     */
    fun checkRefill() {
        // Method implementation
    }

    /**
     * Checks whether a given position on the game board is valid for placing a tile.
     *
     * Preconditions:
     * - A running game (`currentGame`) must exist.
     * - The position must not already be occupied by another tile.
     * - If it's not the first tile, it must be adjacent to an already placed tile.
     *
     * Postconditions:
     * - Returns `true` if the position is free and satisfies the placement rules.
     * - Returns `false` if the position is already occupied or invalid.
     *
     * @param position The position on the game board to validate.
     *
     * @return `true` if the position is valid, otherwise `false`.
     *
     * @throws IllegalStateException If no game is currently active (`currentGame == null`).
     *
     * @sample validatePosition(Coordinate(2, 3))
     */
    fun validatePosition(position: Coordinate): Boolean {
        return true //placeholder
    }

    /**
     * Checks all open tasks of the current player and updates their status.
     * This method is usually called at the end of a turn to see if any new tiles
     * have completed more tasks.
     *
     * Preconditions:
     * - A running game (`currentGame`) must exist.
     *
     * Postconditions:
     * - All tasks of the current player are checked again and marked as completed if possible.
     * - The related refresh method is called so the GUI shows the updated task status.
     *
     * @throws IllegalStateException If no game is currently active (`currentGame == null`).
     *
     * @sample updateTasks()
     */
    fun updateTasks(): Unit {
        // Method implementation
    }
    /**
     * Gets all valid moves for the active player in a *specific game state*.
     * This version is used by MCTS in [HardBotService] to explore hypothetical scenarios.
     *
     * @param game The hypothetical [NovaLunaGame] state to analyze.
     * @return A list of all valid [Move] objects for the active player in the given state.
     */
    fun getPossibleMovesForState(game: NovaLunaGame): List<Move> {
        // TODO: Implement the same logic as getPossibleMovesForCurrentPlayer,

        // Placeholder return
        return emptyList()
    }

    /**
     * Gets all valid moves for the currently active player in the *current game*.
     * This method is the single source of truth for move validation for the UI and simple bots.
     *
     * Preconditions:
     * - A game must be in progress (`rootService.currentGame != null`).
     * - A player must be active.
     * - The game must not be over.
     *
     * @return A list of all valid [Move] objects. The list can be empty if no moves are possible.
     * @throws IllegalStateException if no game is currently running.
     */
    fun getPossibleMovesForCurrentPlayer(): List<Move> {
        val game = rootService.currentGame ?: throw IllegalStateException("No game in progress.")
        return getPossibleMovesForState(game)
    }

    /**
     * Updates the meeple and player marker positions after a tile is selected.
     *
     * This method moves the shared meeple to the spot of the `selectedTile` on the moon wheel
     * and advances the current player's marker on the moon track by the tile's time cost.
     * It is typically called as part of a player's turn execution.
     *
     * Preconditions:
     * - A game must be in progress.
     * - The `selectedTile` was a valid choice in the preceding state.
     *
     * Postconditions:
     * - `NovaLunaGame.meeplePosition` is updated.
     * - The current player's `moonTrackPosition` is increased.
     * - A UI refresh is triggered.
     *
     * @param selectedTile The tile that the current player just took from the moon wheel.
     * @throws IllegalStateException if no game is currently running.
     */
    fun moveMeeple(selectedTile: Tile) {
        // TODO: Implement logic to update meeple position and player's moon track marker.
    }
}