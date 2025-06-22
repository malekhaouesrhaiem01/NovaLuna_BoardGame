package service

import entity.NovaLunaGame
import entity.Tile
import entity.TileColour

class TileLoader {

    fun readTiles(filePath : String = "/nl_tiles.csv") : MutableList<Tile> {
        // search for nl_tiles.csv in resources
        val fileURL = object {}.javaClass.getResourceAsStream( filePath )
            ?: throw IllegalArgumentException("file not found: $filePath")

        val novaLunaTiles = mutableListOf<Tile>()

        fileURL.bufferedReader().useLines { lines ->
            // drop the first line with column names
            lines.drop(1).forEach { line ->
                val segments = line.split(",")
                val id = segments[0].toInt()
                val tileColour = stringToTileColour(segments[1].toString())
                val time = segments[2].toInt()
                val task1 = segments[3].toString().mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()
                val task2 = segments[4].toString().mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()
                val task3 = segments[5].toString().mapNotNull{ colourMap[it] }.groupingBy { it }.eachCount()

                novaLunaTiles.add(
                    Tile(
                        id = id,
                        time = time,
                        tileColour = tileColour,
                        tasks = mutableListOf(task1, task2, task3),
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
        when (string) {
            "cyan" -> return TileColour.CYAN
            "blue" -> return TileColour.BLUE
            "red" -> return TileColour.RED
            "yellow" -> return TileColour.YELLOW
            else -> throw IllegalArgumentException("Invalid TileColour: $string")
        }
    }

    val colourMap = mapOf(
       'c' to TileColour.CYAN,
        'b' to TileColour.BLUE,
        'r' to TileColour.RED,
        'y' to TileColour.YELLOW
    )

}