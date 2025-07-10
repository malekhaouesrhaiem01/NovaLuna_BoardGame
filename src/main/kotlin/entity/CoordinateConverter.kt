package entity

import tools.aqua.bgw.util.Coordinate as BGWCoordinate

/**
 * Converts a SerializableCoordinate to a BGW Coordinate.
 */
fun SerializableCoordinate.toBGWCoordinate(): BGWCoordinate {
    return BGWCoordinate(xCoord = this.x, yCoord = this.y)
}

/**
 * Converts a BGW Coordinate to a SerializableCoordinate.
 */
fun BGWCoordinate.toSerializableCoordinate(): SerializableCoordinate {
    return SerializableCoordinate(x = this.xCoord, y = this.yCoord)
}
