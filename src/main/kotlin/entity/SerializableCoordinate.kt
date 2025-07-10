package entity

import kotlinx.serialization.Serializable

/**
 * Serializable Coordinate class for Nova Luna.
 *
 * Double for easier conversion to/from BGW Coordinate.
 *
 */
@Serializable
data class SerializableCoordinate(val x: Double, val y: Double)
