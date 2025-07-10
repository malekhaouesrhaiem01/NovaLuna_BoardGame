package entity
import kotlinx.serialization.Serializable

/**
 * Represents a move in the game Nova Luna.
 *
 * @property tile The tile that will be moved.
 * @property position The position the tile will be moved to.
 */
@Serializable
data class Move(val tile: Tile?,
                val position: SerializableCoordinate)
