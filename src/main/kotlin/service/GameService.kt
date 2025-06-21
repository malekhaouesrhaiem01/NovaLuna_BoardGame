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

        val game = checkNotNull(rootService.currentGame)

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
        checkNotNull(game) { "No game is currently running." }

        checkEndGame()

        onAllRefreshables { refreshAfterStartTurn() }
    }

    /**
     * The method [endTurn] ends the current Players turn,
     * as well as changing the current Player to the Player next in line.
     * Calls up the refreshables to update the GUI Scenery.
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
        val game = rootService.currentGame
        checkNotNull(game)

        var currentPlayer = game.players.first()
        for (player in game.players){
            if(player.moonTrackPosition < currentPlayer.moonTrackPosition){
                currentPlayer = player
            } else if(player.moonTrackPosition == currentPlayer.moonTrackPosition){
                if(player.height > currentPlayer.height){
                    currentPlayer = player
                }
            }
        }
        game.activePlayer =  game.players.indexOf(currentPlayer)

        onAllRefreshables { refreshAfterEndTurn() }
    }

    /**
    * Ends the current Nova Luna game by determining the winner,
    * displaying player scores, and clearing the game state.
    *
    * This method is  called from [checkEndGame] when an end condition is met.
    * Triggers [refreshAfterGameEnd] to update the UI with the winner name and players  scores  .
    * @throws IllegalStateException if no game is currently active or the game is already ended
    */
    fun endGame(){
        // Passiert hier irgendwas auf Entity-Ebene?
        // Eigentlich muss doch nur auf GUI Ebene die Anzahl der Tokens
        // der einzelnen Spieler angezeigt werden
        onAllRefreshables { refreshAfterRageQuit() }
        rootService.currentGame = null
    }


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
        val game = rootService.currentGame
        checkNotNull(game)

        if (game.tileTrack.size >= 3) return

        rootService.playerActionService.refillWheel()

        if (game.tileTrack.size < 3 && game.drawPile.isEmpty())
        {
            endGame()
        }
    }

    /**
     * Calculates all valid positions where the current player can place a new tile.
     *
     * Preconditions:
     * - A running game (`currentGame`) must exist.
     *
     * Postconditions:
     * - Returns a list of coordinates that are free and directly next to already places tiles.
     * - If the player has no tiles the list will contain only (0,0)
     *
     * @return A list oft valid positions for placing a tile.
     *
     * @throws IllegalStateException If no game is currently active (`currentGame == null`).
     *
     * @sample getPossiblePosition()
     */
    fun getPossiblePosition(): List<Coordinate>
    {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }
        val player = game.players[game.activePlayer]

        //Liste mit allen Positionen die schon belegt sind
        val occupied = mutableListOf<Coordinate>()
        for (tile in player.tiles)
        {
            if(tile.position != null)
            {
                occupied.add(tile.position!!)
            }
        }

        //Wenn noch kein Tile gelegt wurde, also erstes Tile, dann direkt 0,0
        if( occupied.isEmpty()) return listOf(Coordinate(0, 0))

        //Die Liste, welche returned wird.
        val possible = mutableListOf<Coordinate>()

        // Alle Nachbar Coodinates der bereits belegten Tiles
        for (pos in occupied)
        {
            val neighbors = listOf(
                Coordinate(pos.xCoord + 1, pos.yCoord),
                Coordinate(pos.xCoord - 1, pos.yCoord),
                Coordinate(pos.xCoord, pos.yCoord + 1),
                Coordinate(pos.xCoord, pos.yCoord - 1),
            )

            //Hier wird ermitteltet, welche Nachbar Positionen frei sind
            for (neighbor in neighbors)
            {
                if(!occupied.contains(neighbor) && !possible.contains(neighbor))
                {
                    possible.add(neighbor)
                }
            }
        }

        return possible


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
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        val visitedTiles  =  mutableListOf<Tile>()
        val colorMap = mutableMapOf<entity.TileColour, Int>()

        // For every Tile update the remaining Task that need to be solved
        for(tile in game.players[game.activePlayer].tiles){
            if (tile.tasks.isNotEmpty()) {
                // Get Surrounding Tiles and count the amount of colors it has as neighbours and store it in the Map colorMap
                checkSurroundingTiles(tile,  visitedTiles, colorMap)
            }

            // Mark new Task that are fulfilled by removing them from the Task list of the Tile
            var solvedTask = false
            for(task in tile.tasks){
                for (key in task.keys){
                    try {
                        if(task[key]!! > colorMap[key]!!){
                            solvedTask = false
                            break
                        } else {
                            solvedTask = true
                        }
                    } catch (_ : NoSuchElementException){}

                }
                if(solvedTask){tile.tasks.remove(task)}
            }
        }
    }

    private fun checkSurroundingTiles(tile :Tile, visitedTiles : MutableList<Tile>, colorMap : MutableMap<entity.TileColour, Int>) : MutableMap<entity.TileColour, Int> {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        val coordinate = tile.position
        val neighbors = mutableListOf<Tile>()

        // Get neighbour Tile
        // refTile == reference Tile
        for(refTile in game.players[game.activePlayer].tiles){
            when (refTile.position) {
                Coordinate(coordinate!!.xCoord + 1, coordinate.yCoord) -> neighbors.add(refTile)
                Coordinate(coordinate.xCoord - 1, coordinate.yCoord) -> neighbors.add(refTile)
                Coordinate(coordinate.xCoord, coordinate.yCoord + 1) -> neighbors.add(refTile)
                Coordinate(coordinate.xCoord, coordinate.yCoord - 1) -> neighbors.add(refTile)
            }

            //Checks for a sequence of the same color
            if(tile.tileColour == refTile.tileColour && neighbors.contains(refTile)){
                visitedTiles.add(refTile)
                if(!colorMap.containsKey(refTile.tileColour)){
                    colorMap.put(refTile.tileColour, 1)
                } else {
                    // Store the different colors and the amount the Tile has as a neighbour
                    val temp = colorMap[refTile.tileColour]
                    colorMap[refTile.tileColour] = temp!! + 1
                }
                checkSurroundingTiles(refTile, visitedTiles, colorMap)
            }
        }
        return colorMap
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
     * @throws IllegalStateException if no game is currently running or Tile that is not on the Tile Track is selected.
     */
    fun moveMeepleAndPlayer(selectedTile: Tile) {
        val game =  rootService.currentGame
        checkNotNull(game)


        val currentPlayer =  game.players[game.activePlayer]
        val newMeeplePos = selectedTile.moonTrackPosition
        checkNotNull(newMeeplePos)
        val stepsForPlayer =  selectedTile.time


        //Update Meeple Position and Remove Tile from that Position
        game.meeplePosition = newMeeplePos
        game.tileTrack.remove(selectedTile)
        selectedTile.moonTrackPosition = null



        currentPlayer.moonTrackPosition += stepsForPlayer
        //changes the height of the currentPlayer, for the case two Players are at the same Position
        for(player in game.players){
            /* For Every Player that's already in that moonTrackposition, add 1 additional height for the currentPlayer
             * sind he came last. Therefore, if two Players are at the same position the currentPlayer would have a height
             * of two making him the Player next in Line.
             */
            if(player.moonTrackPosition == currentPlayer.moonTrackPosition){
                currentPlayer.height++
            }
        }
        // remove 1 height, because we add 1 height for every Player arriving in a new Position
        currentPlayer.height -= 1

        // onAllRefreshables { refreshAfterMoveMeepleAndPlayer() }
    }
}