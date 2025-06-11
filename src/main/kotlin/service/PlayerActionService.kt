package service
import tools.aqua.bgw.util.Coordinate
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService()  {

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
    fun playTile( moonwheelIndex: Int, position: Coordinate){}
}