package service
import entity.Move
import tools.aqua.bgw.util.Coordinate

/**
 * Service layer class that provides the logic for the possible actions a Player in
 * Nova Luna can take.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
open class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Allows the current player to select a tile from the Moon Wheel at the specified index
     * and place it on their personal space  at the desired position
     * the Meeple is  then moved to that index.
     * The player's time marker is advanced based on the tile's cost.
     * Triggers [refreshAfterTilePlayed] to update the UI accordingly.
     *
     * @param tileTrackIndex The index of the tile to take from the tile track (must be one of the next 3 tiles).
     * @param position The coordinate where the tile should be placed on the current player's space .
     *
     * @throws IllegalStateException If no game is active or it's not the current player's turn.
     * @throws IllegalArgumentException If the selected tile is invalid or cannot be placed at the given position.
     */
    fun playTile(tileTrackIndex: Int, position: Coordinate) {
        val game = checkNotNull(rootService.currentGame)

        println("   [playTile] START - ${game.players[game.activePlayer].playerName}")
        println("   - Heights: ${game.players.map { "${it.playerName}:${it.height}" }}")
        // Get the selected tile from the tile track
        val selectedTile = game.tileTrack[tileTrackIndex]
        checkNotNull(selectedTile)

        // store the tile ID before moving the tile (for sendTurnMessage below)
        val tileId = selectedTile.id

        // add the selected tile to the list of tiles of the current player
        game.players[game.activePlayer].tiles.add(selectedTile)
        // add the coordinates where the tile is placed to the tile
        selectedTile.position = position

        // update position on the moon wheel of the token
        // and the position of the meeple on the tile track
        rootService.gameService.moveMeepleAndPlayer(selectedTile)
        println("   [playTile] AFTER moveMeepleAndPlayer")
        println("   - Heights: ${game.players.map { "${it.playerName}:${it.height}" }}")

        // check if tasks are now fulfilled
        rootService.gameService.updateTasks()
        println("   [playTile] AFTER updateTasks")
        println("   - Heights: ${game.players.map { "${it.playerName}:${it.height}" }}")
        // Send network message if it's a network game and it's our turn
        if (rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN) {
            println("    Sending network message...")

            rootService.networkService.sendTurnMessage(
                tileId = tileId,
                x = position.xCoord.toInt(),
                y = position.yCoord.toInt(),
                refillTrack = game.refilledThisTurn
            )
            println("    Network message sent")
            println("    NOT sending network message (state: ${rootService.networkService.connectionState})")

        }

        // check if game conditions are fulfilled to end the game
        // if not just refresh after a tile ist played
        if(rootService.gameService.checkEndGame()){
            rootService.gameService.endGame(game.players[game.activePlayer])

            onAllRefreshables { refreshAfterGameEnd(game.players[game.activePlayer]) }
        }
        else{
            onAllRefreshables { refreshAfterTilePlayed() }
        }
    }


    /**
     * Executes a move for the current player based on a [Move] object.
     * This method is primarily used by the bot services. It finds the corresponding
     * tile on the moon wheel and calls the primary `playTile` method.
     *
     * @param move The [Move] object containing the tile to play and its target position.
     * @throws IllegalStateException If no game is active or the tile from the move is not available.
     * @throws IllegalArgumentException If the move is invalid.
     */
    open fun playTile(move: Move) {
        val game = checkNotNull(rootService.currentGame)
        // get the index of the selected tile in the [Move] object
        val tileIndex = game.tileTrack.indexOf(move.tile)
        // calls the main playTile function to play the selected move from the bot
        playTile(tileIndex, move.position)

    }

    /**
     * Lets the player undo their last move, if that's allowed in the current mode.
     *
     * No effect in online games (not available in GUI) or when there's nothing to undo.
     *
     * Preconditions:
     * - There must be an active game running.
     * - A player has to be currently taking their turn.
     * - The game must be offline.
     * - There has to be a previous game state saved.
     *
     * Postconditions:
     * - Restores the game to the previous state.
     * - The current player gets to act again.
     * - Calls refresh methods like `refreshAfterUndo()`.
     *
     * @returns This method has no return value (`Unit`).
     *
     * @throws IllegalStateException If no game is running.
     * @throws NoSuchElementException If there's no undo state to go back to.
     */
    fun undo() {
        val game = checkNotNull(rootService.currentGame)


        rootService.currentGame = game.previousState

    }


    /**
     * Reapplies a move that was previously undone, if there’s one to redo.
     *
     * No effect in online games (not available in GUI) and won’t do anything if the redo stack is empty.
     *
     * Preconditions:
     * - There must be an active game running.
     * - A player has to be currently taking their turn.
     * - The game must be offline.
     * - There must be a redoable game state.
     *
     * Postconditions:
     * - Restores the next state from the redo stack.
     * - The current player can act again.
     * - Calls refresh methods like `refreshAfterRedo()`.
     *
     * @returns This method has no return value (`Unit`).
     *
     * @throws IllegalStateException If no game is running.
     * @throws NoSuchElementException If there’s nothing to redo.
     */
    fun redo() {
        //Method implementation
    }

    /**
     * Saves the current game state to a file or storage so it can be loaded later.
     *
     * Only works in offline mode(not available in GUI) Doesn’t change the game, just writes it out.
     *
     * Preconditions:
     * - The game must be offline.
     * - A player has to be currently taking their turn.
     * - There must be an active game running.
     *
     * Postconditions:
     * - The full game state is saved.
     * - Nothing about the current game session changes.
     *
     * @returns This method has no return value (`Unit`).
     *
     * @throws IllegalStateException If no game is running or if the game is online.
     * @throws IOException If saving fails (example: due to file access issues).
     */
    fun save() {
        //Method implementation
    }

    /**
     * Loads a previously saved game state and restores it completely.
     *
     * Only works in offline mode (not available in GUI). Brings back players, tiles, meeple position; everything.
     *
     * Preconditions:
     * - The game must be offline.
     * - A valid saved game must exist.
     *
     * Postconditions:
     * - The saved state is fully loaded and becomes the current game state.
     * - Players, tile layout, and turn order are restored.
     * - May trigger refresh methods like `refreshAfterGameStart()` and `refreshAfterTurnStart()`.
     *
     * @returns This method has no return value (`Unit`).
     *
     * @throws FileNotFoundException If the saved game can’t be found.
     * @throws IOException If something goes wrong while loading.
     */
    fun load() {
        //Method implementation
    }


    /**
     * The method [refillWheel] fills up the moonWheel with cards in the [drawPile]
     * when there are 2 or fewer cards in the moonWheel
     *
     *
     * Preconditions:
     * - Game must be started
     * - It has to be a players Turn
     * - Two or fewer cards in the moonWheel
     *
     * Postconditions:
     * - moonWheel is completely filled up (11 Cards)
     *
     * Exceptions:
     * @throws IllegalStateException is thrown, when no game exists.
     * @throws IllegalArgumentException is thrown, when there are more than 2 cards in the moonWheel.
     */
    fun refillWheel(){
        val game = rootService.currentGame
        checkNotNull(game)

        println("     REFILL: Starting refill")
        println("   - Active player: ${game.activePlayer} (${game.players[game.activePlayer].playerName})")
        println("   - Connection state: ${rootService.networkService.connectionState}")
        println("   - Tiles before refill: ${game.tileTrack.count { it != null }}")

        val filled = game.tileTrack.count {it != null}

        if (filled > 2) return //falls mehr als 2 funktioniert es nicht.

        var index = (game.meeplePosition + 1) % game.tileTrack.size //index startet 1 nach meeple

        repeat(game.tileTrack.size -1) // -1 da Meeple pos nicht gefüllt werden kann
        {
            if (game.tileTrack[index] == null && game.drawPile.isNotEmpty())
            {
                game.tileTrack[index] = game.drawPile.removeAt(0)
            }
            index = (index + 1) % game.tileTrack.size
        }



        // Mark that refill happened this turn
        game.refilledThisTurn = true

        println("   - Tiles after refill: ${game.tileTrack.count { it != null }}")
        println("   - refilledThisTurn flag: ${game.refilledThisTurn}")

        onAllRefreshables { refreshAfterRefill() }
    }
}

