package service

import entity.Tile
import entity.TileColour

/**
 *  Service layer Class that loads the Tiles for a Nova Luna Game
 */
class TileLoader {

    val colourMap = mapOf(
        'c' to TileColour.CYAN,
        'b' to TileColour.BLUE,
        'r' to TileColour.RED,
        'y' to TileColour.YELLOW
    )

    /**
     * Reads the Tiles from the given csv File
     *
     * @param filePath The file of which should be read. The Default here is "/nl_tiles.cvs"
     *
     * Pre-Conditions:
     *
     * Post-Conditions:
     * - All the Tiles in the csv file has been Initialized as a Tile in [Tile]
     *
     * @returns Returns a List with all the NovaLuna Tiles that were read
     *
     * @throws IllegalArgumentException if file can't be found
     * @throws
     */
    fun readTiles(filePath : String = "/nl_tiles.csv") : MutableList<Tile?> {
        // search for nl_tiles.csv in resources
        val fileURL = object {}.javaClass.getResourceAsStream( filePath )
            ?: throw IllegalArgumentException("file not found: $filePath")

        val novaLunaTiles = mutableListOf<Tile?>()

        fileURL.bufferedReader().useLines { lines ->
            // drop the first line with column names
            lines.drop(1).forEach { line ->
                val segments = line.split(",")
                val id = segments[0].toInt()
                val tileColour = stringToTileColour(segments[1])
                val time = segments[2].toInt()
                val task1 = segments[3].mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()
                val task2 = segments[4].mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()
                val task3 = segments[5].mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()

                novaLunaTiles.add(
                    Tile(
                        id = id,
                        time = time,
                        tileColour = tileColour,
                        tasks = listOf(Pair(task1, false), Pair(task2, false), Pair(task3, false)),
                        position = null,
                        moonTrackPosition = null,
                        // Needs to be implemented

                )
                )
            }
        }

        return novaLunaTiles
    }

    private fun stringToTileColour(string : String) : TileColour {
        return when (string) {
            "cyan" -> TileColour.CYAN
            "blue" -> TileColour.BLUE
            "red" -> TileColour.RED
            "yellow" -> TileColour.YELLOW
            else -> throw IllegalArgumentException("Invalid TileColour: $string")
        }
    }

}