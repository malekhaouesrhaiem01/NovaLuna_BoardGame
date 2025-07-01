package entity

import tools.aqua.bgw.util.Coordinate

/**
 * Represents a tile in the game Nova Luna.
 *
 * @property id The id of the Tile to Identify the Tile
 * @property time The time cos of taking the tile.
 * @property tileColour The background color of the tile.
 * @property tasks A list of task maps, where each map links a required [TileColour] to the number of adjacent tiles
 * needed for completion.
 * @property position Coordinates of the tile on a players grid.
 * @property moonTrackPosition the position of the Tile on the MoonTrack
 */
data class Tile(val id : Int,
                val time: Int,
                val tileColour: TileColour,
                var tasks: List<Pair<Map<TileColour, Int>, Boolean>>,
                var position: Coordinate? = null,
                var moonTrackPosition: Int?
)