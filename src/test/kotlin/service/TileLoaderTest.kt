package service

import kotlin.test.Test
import kotlin.test.assertTrue

/** Test class to test loading of the Tiles from a csv-data */
class TileLoaderTest {

    @Test
    fun testIfTilesLoad(){

        val rootService = RootService()
        val tiles = rootService.tileLoader.readTiles()
        // check if the number of tiles which got loaded is correct
        assertTrue(tiles.size == 68)
    }


}