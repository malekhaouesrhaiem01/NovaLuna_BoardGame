package service

import entity.*
import kotlin.test.*

/**
 * Test class for method GetAvailableTiles() from class GameService.kt
 */
class GetAvailableTilesTest
{
    private lateinit var  rootService: RootService

    /**
     * setup for a two player Game before each test
     */
    @BeforeTest
    fun setUp()
    {
        rootService = RootService()
        val players = listOf(
            Player("Player1",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                1),
            Player("Player2",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.ORANGE,
                mutableListOf(),
                1)
        )
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
    }

    /**
     * Tests that the method return the next three tile indices after the meeple position
     * when the wheel is fully populated.
     */
    @Test
    fun testReturnsNextThreeIndices()
    {
        val expected = listOf(1,2,3)
        assertEquals(expected,rootService.gameService.getAvailableTiles())
    }

    /**
     * Tests that the method correctly skips over null positions
     * and still returs three valid tile indices.
     */
    @Test
    fun testSkipsEmptyTilesPosition()
    {
        val game = rootService.currentGame!!
        game.tileTrack[1] = null

        val expected = listOf(2,3,4)
        assertEquals(expected,rootService.gameService.getAvailableTiles())
    }

    /**
     * Tests that when fewer than three tiles are left on the wheel,
     * only those available tile indices are returned.
     */
    @Test
    fun testOnlyTwoTilesLeft()
    {
        val game = rootService.currentGame!!
        for(i in 1 until game.tileTrack.size)
        {
            if(i != 5 && i != 7) game.tileTrack[i] = null
        }

        val expected = listOf(5,7)
        assertEquals(expected,rootService.gameService.getAvailableTiles())
    }

    /**
     * Tests that the function works correctly
     * if the range of the next three tiles goes over the top middle.
     */
    @Test
    fun testCircularTileTrack(){
        val game = rootService.currentGame!!
        game.meeplePosition = 8

        for(i in 1 until game.tileTrack.size)
        {
            val bool = i != 5 && i != 7 && i != 6 && i != 9
            if(bool) game.tileTrack[i] = null
        }

        val expected = listOf(9,5,6)
        assertEquals(expected,rootService.gameService.getAvailableTiles())
    }
}