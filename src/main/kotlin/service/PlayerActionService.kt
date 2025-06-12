package service

import tools.aqua.bgw.util.Coordinate

class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Allows the current player to select a tile from the Moon Wheel at the specified index
     * and place it on their personal space  at the desired position
     * the Meeple is  then moved to that index.
     * The player's time marker is advanced based on the tile's cost.
     * Triggers [refreshAfterTilePlayed] to update the UI accordingly.
     *
     * @param moonwheelIndex The index of the tile to take from the Moon Wheel (must be one of the next 3 tiles).
     * @param position The coordinate where the tile should be placed on the current player's space .
     *
     * @throws IllegalStateException If no game is active or it's not the current player's turn.
     * @throws IllegalArgumentException If the selected tile is invalid or cannot be placed at the given position.
     */
    fun playTile(moonwheelIndex: Int, position: Coordinate) {}


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
        // Method implementation
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
     * - moonWheel is completely filled up (12 Cards)
     *
     * Exceptions:
     * @throws IllegalStateException is thrown, when no game exists.
     * @throws IllegalArgumentException is thrown, when there are more than 2 cards in the moonWheel.
     */
    fun refillWheel(){

    }
}

