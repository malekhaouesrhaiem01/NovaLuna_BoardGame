package service

import entity.NovaLunaGame
import entity.Player

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
    * Ends the current Nova Luna game by determining the winner,
    * displaying player scores, and clearing the game state.
    *
    * This method is  called from [checkEndGame] when an end condition is met.
    * Triggers [refreshAfterGameEnd] to update the UI with the winner name and players  scores  .
    * @throws IllegalStateException if no game is currently active or the game is already ended
    */
    fun endGame(){}

    /**
     * Saves the current Nova Luna game state to a file or persistent storage.
     *
     * Only allowed in offline mode while a game is active
     *
     * @throws IllegalStateException if no game is active or the game is in network mode
     * @throws IOException if an error occurs during saving
     */
    fun saveGame(){}





}