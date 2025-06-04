package entity

import tools.aqua.bgw.util.Coordinate

/**
 * Represents a tile in the game Nova Luna.
 *
 * @property time The time cos of taking the tile.
 * @property tileColour The background color of the tile.
 * @property tasks A list of task maps, where each map links a required [TileColour] to the number of adjacent tiles
 * needed for completion.
 * @property position Coordiantes of the tile on a players grid.
 */
data class Tile(val time: Int,
                val tileColour: TileColour,
                val tasks: List<Map<TileColour, Int>>,
                var position: Coordinate? = null
)