package service

import entity.*

/**
 * Service layer class that provides the logic for actions not
 * directly related to a single Player
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
open class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Starts a new Nova Luna game with the given players and simulation speed.
     * the game state + drawPile + tile track are initialized.
     * Triggers [gui.NovaApplication.refreshAfterStartGame] to update the UI with the initial game state.
     * @param players The list of players participating in the game (2 to 4 players).
     * @param simulationSpeed The speed of the game simulation (0 to 10)
     * @throws IllegalStateException If a game is already running.
     * @throws IllegalArgumentException or if the simulation speed is greater than 10.
     * @throws IllegalArgumentException If the number of players is not between 2 and 4
     */
    fun startNewGame(players : List<Player>, simulationSpeed : Int, randomOrder : Boolean = false,
                     firstGame : Boolean,
                     startTurnImmediately: Boolean = true){

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
        val tileTrack: MutableList<Tile?> = drawPile.subList(0, 11).toMutableList()
        drawPile.subList(0, 11).clear()
        // First index is null because there is no tile but the meeple
        tileTrack.add(0, null)

        var playersOrder = players
        // setting the start order random when randomOrder = true
        if (randomOrder) {
            playersOrder = players.shuffled()
        }
        // setting heights according to beginning order

        for (i in 0 until playersOrder.size) {
            playersOrder[i].height = playersOrder.size - i
        }

        val game = NovaLunaGame(
            activePlayer = 0,
            meeplePosition = 0,
            simulationSpeed = simulationSpeed,
            players = playersOrder.toMutableList(),
            drawPile = drawPile,
            tileTrack = tileTrack,
            firstGame = firstGame
        )

        rootService.currentGame = game

        // Save the initial game state for undo functionality
        rootService.playerActionService.saveInitialGameState()

        onAllRefreshables { refreshAfterStartGame()}
        if(startTurnImmediately){
            startTurn()
        }
    }

    /**
     * Starts a new Nova Luna game for network play with a predefined tile order.
     * Used by NetworkService to ensure all players have the same tile arrangement.
     *
     * @param players The list of players
     * @param simulationSpeed The simulation speed
     * @param tileIds The exact order of tile IDs to use (no shuffling)
     * @param isFirstGame Whether this is the first game (affects token count)
     */
    fun startNetworkGame(players: List<Player>, simulationSpeed: Int, tileIds: List<Int>, isFirstGame: Boolean,
                         startTurnImmediately: Boolean = true) {
        require(players.size in 2..4) { "Spieleranzahl muss zwischen 2 und 4 sein." }
        require(simulationSpeed < 11) { "SimulationSpeed darf maximal 10 sein" }

        if (rootService.currentGame != null) {
            throw IllegalStateException("A game is already running.")
        }

        // Convert tile IDs to actual tiles in the exact order provided
        val allTiles = rootService.tileLoader.readTiles().filterNotNull()
        val drawPile: MutableList<Tile?> = tileIds.map { id ->
            allTiles.find { it.id == id } ?: error("Tile with ID $id not found")
        }.toMutableList<Tile?>()  // Explicitly specify nullable type

        drawPile.reverse()

        // Initialize tileTrack with first 11 tiles
        val tileTrack: MutableList<Tile?> = drawPile.take(11).toMutableList()
        drawPile.subList(0, 11).clear()
        tileTrack.add(0, null) // Meeple position

        // Set player heights based on order
        for (i in 0 until players.size) {
            players[i].height = players.size - i
        }

        // Determine token count based on first game rules
        val tokenCount = if (isFirstGame) when (players.size) {
            3 -> 18 -1
            4 -> 16 -1
            else -> 21-1
        } else 21 -1

        // Update player token counts
        players.forEach { it.tokenCount = tokenCount }

        val game = NovaLunaGame(
            activePlayer = 0,
            meeplePosition = 0,
            simulationSpeed = simulationSpeed,
            players = players.toMutableList(),
            drawPile = drawPile,
            tileTrack = tileTrack,
            firstGame = isFirstGame
        )

        rootService.currentGame = game
        
        // Save the initial game state for undo functionality
        rootService.playerActionService.saveInitialGameState()
        
        onAllRefreshables { refreshAfterStartGame() }
        if(startTurnImmediately){
            startTurn()
        }
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

        var tileTrackIsEmpty = true
        for (tile in game.tileTrack){
            if (tile != null){
                tileTrackIsEmpty = false
                break
            }
        }

        return game.players[game.activePlayer].tokenCount < 1 ||
                (tileTrackIsEmpty && game.drawPile.isEmpty())
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

//        println("   [startTurn] BEFORE clone:")
//        game.players.forEachIndexed { idx, p ->
//            println("     Player $idx (${p.playerName}): pos=${p.moonTrackPosition}, height=${p.height}")
//        }

        // Reset the turn flags for the new turn
        game.hasPlayedThisTurn = false
        game.refilledThisTurn = false

        var checkAutoRefill = true
        for (tile in game.tileTrack){
            if (tile != null) {
                checkAutoRefill = false
                break
            }
        }
        if (checkAutoRefill) {
            rootService.playerActionService.refillWheel()
        }

        onAllRefreshables { refreshAfterStartTurn() }

        val player = game.players[game.activePlayer]

        if(player.playerType == PlayerType.EASYBOT) {
            rootService.easyBotService.executeEasyMove()
        } else if(player.playerType == PlayerType.HARDBOT) {
            rootService.hardBotService.executeHardBotMove()
        }

        //println("   [startTurn] AFTER clone:")
    }

    /**
     * Restores the turn state after an undo/redo operation.
     * Performs necessary checks like auto-refill but does not trigger bot moves.
     * Also preserves the hasPlayedThisTurn flag from the restored state.
     * 
     * @throws IllegalStateException If no game is currently running.
     */
    fun restoreTurnStateWithoutBot() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        // Don't reset hasPlayedThisTurn - preserve it from the restored state
        // Don't reset refilledThisTurn - preserve it from the restored state

        // Check if auto-refill is needed (when all tiles are null)
        var checkAutoRefill = true
        for (tile in game.tileTrack) {
            if (tile != null) {
                checkAutoRefill = false
                break
            }
        }
        if (checkAutoRefill) {
            rootService.playerActionService.refillWheel()
        }
    }

    /**
     * Restores the turn state after an undo/redo operation.
     * Performs necessary checks like auto-refill and triggers bot moves if needed.
     * Also preserves the hasPlayedThisTurn flag from the restored state.
     * 
     * @throws IllegalStateException If no game is currently running.
     */
    fun restoreTurnState() {
        restoreTurnStateWithoutBot()
        
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        // If it's a bot's turn after restore, trigger the bot to make its move
        val player = game.players[game.activePlayer]
        if (!game.hasPlayedThisTurn) { // Only trigger if no move has been made this turn
            if (player.playerType == PlayerType.EASYBOT) {
                rootService.easyBotService.executeEasyMove()
            } else if (player.playerType == PlayerType.HARDBOT) {
                rootService.hardBotService.executeHardBotMove()
            }
        }
    }

    //private fun saveForUndoRedo(game : NovaLunaGame) {}

    /**
     * The method [endTurn] ends the current Players turn,
     * as well as changing the current Player to the Player next in line.
     * Calls up the refreshable to update the GUI Scenery.
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

        //println("   END TURN:")
        //println("   - Current player: ${game.activePlayer} (${game.players[game.activePlayer].playerName})")

        if(checkEndGame()){
            //println("   - Game ending detected!")

            val winner = game.players[game.activePlayer]

            endGame(winner)
            return
        }

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
    * Triggers [gui.NovaApplication.refreshAfterGameEnd] to update the UI with the winner name and players  scores  .
    * @throws IllegalStateException if no game is currently active or the game is already ended
    */
    fun endGame(winner: Player){
        // Passiert hier irgendwas auf Entity-Ebene?
        // Eigentlich muss doch nur auf GUI Ebene die Anzahl der Tokens
        // der einzelnen Spieler angezeigt werden
        onAllRefreshables { refreshAfterGameEnd(winner) }
        
        // Reset game state and clear undo/redo history
        rootService.playerActionService.resetGameState()
        rootService.currentGame = null
    }

    

    /**
     * Returns a list of the indices of the next three positions after the Meeple on the selection track.
     *
     * Rules:
     * - If a tile is present at the position, its index is added to the list.
     * - If the position is empty, the position will be skipped.
     * - Wrapping is handled using modulo to loop back the beginning of the track if necessary.
     *
     * Preconditions:
     * - A running Game must exist.
     *
     * Postconditions:
     * - The returned list has exactly three elements (indices or nulls).
     *
     * @throws IllegalStateException If no game is currently active.
     *
     * @return A list of the next max. 3 available Tiles elements, each being a valid index Int.
     *
     * @sample getAvailableTiles()
     */
    fun getAvailableTiles(): List<Int>
    {
        val game = rootService.currentGame
        checkNotNull(game)

        val track = game.tileTrack
        val result = mutableListOf<Int>()

        var pos = game.meeplePosition
        var checked = 0

        while(result.size < 3 && checked < track.size -1) //result ist max 3 oder alle pos abgecheckt.
        {
            pos = (pos +1) % track.size

            if(track[pos] != null) //nur wenn Tiles vorhanden und bei null wird geskippt.
            {
                result += pos
            }
            checked++
        }
        return result

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
    fun getPossiblePosition(): List<SerializableCoordinate>
    {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        val player = game.players[game.activePlayer]

        //Liste mit allen Positionen die schon belegt sind
        val occupied = mutableListOf<SerializableCoordinate>()
        for (tile in player.tiles)
        {
            if(tile?.position != null) {
                val position = tile.position
                checkNotNull(position)
                occupied.add(position)
            }
        }

        //Wenn noch kein Tile gelegt wurde, also erstes Tile, dann direkt 0,0
        if( occupied.isEmpty()) return listOf(SerializableCoordinate(0.0, 0.0))

        //Die Liste, welche returned wird.
        val possible = mutableListOf<SerializableCoordinate>()

        // Alle Nachbar Coordinates der bereits belegten Tiles
        for (pos in occupied)
        {
            val neighbors = listOf(
                SerializableCoordinate(pos.x + 1, pos.y),
                SerializableCoordinate(pos.x - 1, pos.y),
                SerializableCoordinate(pos.x, pos.y + 1),
                SerializableCoordinate(pos.x, pos.y - 1),
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
    fun updateTasks() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running."}

        //Iterate through every Tile in Players hand
        for (tile in game.players[game.activePlayer].tiles){
            val visitedTiles = mutableListOf<Tile>()
            val colorMap = mutableMapOf<TileColour, Int>()
            if (tile != null){
                // Check for the number of the colors that are neighbours of that Tile
                checkSurroundingTiles(tile, visitedTiles, colorMap)
                val pairList = mutableListOf<Pair<Map<TileColour, Int> , Boolean>>()
                //For every Task check if its completed
                for (taskPair in tile.tasks){
                    val booleanList = mutableListOf<Boolean>()

                    // Get the Task from the Task Pair
                    val task = taskPair.first

                    // If Task is not Empty
                    if (task.isNotEmpty()){
                        // Check for every color in the Task
                        for(color in task.keys){
                            // If the colorMap contains it, If yes
                            if (colorMap.contains(color)){

                                // then check if Task in is higher than colorMap Int, for the corresponding color
                                if(task.getValue(color) > colorMap.getValue(color)){

                                    // add false since Task isn't completed
                                    booleanList.add(false)
                                } else {
                                    // add true since Task is completed
                                    booleanList.add(true)
                                }
                            } else {
                                // If No, then add false since Tile doesn't have that color as Neighbour
                                booleanList.add(false)
                            }
                        }
                        pairList.add(createPair(task, booleanList))

                    } else {
                        pairList.add(Pair(task, false))
                    }
                }
                // Set Updated Task List for the Tile
                tile.tasks = pairList
            }
        }

        updateTokens()
    }

    private fun updateTokens(){
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }
        var count = 0
        for(tile in game.players[game.activePlayer].tiles){
            checkNotNull(tile)
            for(task in tile.tasks){
                if (task.second){
                    count++
                }
            }
        }
        if (game.firstGame){ //Firstgame == true
            when(game.players.size){
                2 -> game.players[game.activePlayer].tokenCount = 16 - 1 - count
                3 -> game.players[game.activePlayer].tokenCount = 18 - 1 - count
                4 -> game.players[game.activePlayer].tokenCount = 21 - 1 - count
            }
        } else{
            game.players[game.activePlayer].tokenCount = 21 - 1 - count
        }
        if (game.players[game.activePlayer].tokenCount < 0){
            game.players[game.activePlayer].tokenCount = 0
        }
    }

    private fun createPair(task : Map<TileColour, Int> , booleanList: List<Boolean>) :
            Pair<Map<TileColour, Int>, Boolean> {
        // Return a Pair with True if Boolean List contains no false inside else return false
        return if (booleanList.contains(false)){
            Pair(task, false)
        } else {
            Pair(task, true)
        }
    }


    private fun checkSurroundingTiles(tile :Tile, visitedTiles : MutableList<Tile>,
                                      colorMap : MutableMap<TileColour, Int>) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running." }

        val neighbors = getNeighbours(tile, visitedTiles)

        // For Every Neighbour Tile
        for(neighborTile in neighbors){
            // Check if visited. If not then add 1 count to color as neighbour
           if (!visitedTiles.contains(neighborTile)){
                if (!colorMap.contains(neighborTile.tileColour)){
                    colorMap.put(neighborTile.tileColour, 1)
                } else {
                    val temp = colorMap[neighborTile.tileColour]
                    checkNotNull(temp)
                    colorMap.replace(neighborTile.tileColour, temp, temp + 1)
                }
            }
            visitedTiles.add(neighborTile)

            checkForSequence(neighborTile, visitedTiles, colorMap)
        }
    }

    private fun checkForSequence(tile :Tile, visitedTiles : MutableList<Tile>, colorMap : MutableMap<TileColour, Int>){
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running."}

        val neighbors = getNeighbours(tile, visitedTiles)

        for(neighborTile in neighbors){
            if(neighborTile.tileColour == tile.tileColour && !visitedTiles.contains(neighborTile)){
                val temp = colorMap[neighborTile.tileColour]
                checkNotNull(temp)
                colorMap.replace(neighborTile.tileColour, temp, temp + 1)
                checkForSequence(neighborTile, visitedTiles, colorMap)
            }
        }
    }

    private fun getNeighbours(tile :Tile, visitedTiles : MutableList<Tile>) : MutableList<Tile>{
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently running."}
        val neighbors = mutableListOf<Tile>()
        val coordinate = tile.position
        checkNotNull(coordinate)
        visitedTiles.add(tile)

        // Get neighbour Tile
        // neighbourTile
        for(neighbourTile in game.players[game.activePlayer].tiles){
            checkNotNull(neighbourTile)
            try {
                when (neighbourTile.position) {
                    SerializableCoordinate(coordinate.x + 1, coordinate.y) -> neighbors.add(neighbourTile)
                    SerializableCoordinate(coordinate.x - 1, coordinate.y) -> neighbors.add(neighbourTile)
                    SerializableCoordinate(coordinate.x, coordinate.y + 1) -> neighbors.add(neighbourTile)
                    SerializableCoordinate(coordinate.x, coordinate.y - 1) -> neighbors.add(neighbourTile)
                }
            } catch (_ : NullPointerException){}
        }
        return neighbors
    }

//    /**
//     * Gets all valid moves for the active player in a *specific game state*.
//     * This version is used by MCTS in [service.bot.HardBotService] to explore hypothetical scenarios.
//     *
//     * @param game The hypothetical [NovaLunaGame] state to analyze.
//     * @return A list of all valid [Move] objects for the active player in the given state.
//     */
//    fun getPossibleMovesForState(game: NovaLunaGame): List<Move> {
//        // but for the given game state instead of the current one.
//
//        // Placeholder return
//        return emptyList()
//    }

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
    open fun getPossibleMovesForCurrentPlayer(): List<Move> {
        val game = rootService.currentGame ?: throw IllegalStateException("No game in progress.")

        // list of the tiles that a player can select
        val possibleTiles = getAvailableTiles().map{ game.tileTrack[it] }
        // List with all possible coordinates where a tile can be placed
        val possibleCoords = getPossiblePosition()

        val possibleMoves = mutableListOf<Move>()
        for(tile in possibleTiles){
            for (cord in possibleCoords){
                possibleMoves.add(Move(tile, cord))
            }
        }

        return possibleMoves
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
    fun moveMeepleAndPlayer(selectedTile: Tile)
    {
        val game = rootService.currentGame
        checkNotNull(game)

        val currentPlayer = game.players[game.activePlayer]
        val newMeeplePos = game.tileTrack.indexOf(selectedTile)
        val stepsForPlayer = selectedTile.time


        //Update Meeple Position and Remove Tile from that Position
        game.meeplePosition = newMeeplePos
        val index = game.tileTrack.indexOf(selectedTile)
        game.tileTrack.remove(selectedTile)
        game.tileTrack.add(index, null)
        selectedTile.moonTrackPosition = null

        currentPlayer.moonTrackPosition += stepsForPlayer
        currentPlayer.height = 0

        //changes the height of the currentPlayer, for the case two Players are at the same Position
        for (player in game.players)
        {
            /* For Every Player that's already in that moonTrackposition,
             * add 1 additional height for the currentPlayer since he came last.
             * Therefore, if two Players are at the same position the currentPlayer would have a height
             * of two making him the Player next in Line.
             */
            if(player.moonTrackPosition == currentPlayer.moonTrackPosition)
            {
                currentPlayer.height++
            }
        }
        // remove 1 height, because we add 1 height for every Player arriving in a new Position
        //currentPlayer.height -= 1

        }

}
