package entity

import kotlinx.serialization.Serializable

/**
 * An Enum class representing the background color of a tile.
 */
@Serializable
enum class TileColour
{
    RED,
    BLUE,
    CYAN,
    YELLOW
}
