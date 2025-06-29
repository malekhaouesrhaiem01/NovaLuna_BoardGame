package service

import entity.*
import tools.aqua.bgw.util.Coordinate
import kotlin.test.*

/**
 * Test class for method GetPossiblePosition() from class GameService.kt
 */
class GetPossiblePositionTest
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
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false)
    }

    /**
     * Tests that (0,0) is returned when no tiles have been placed yet.
     */
    @Test
    fun testReturnsZeroForFirstTile()
    {
        val expected = listOf(Coordinate(0,0))
        assertEquals(expected, rootService.gameService.getPossiblePosition())
    }

    /**
     * Tests that all 4 direct neighbors are returned for a single placed tile at (0,0).
     */
    @Test
    fun testReturnsFourNeighbours()
    {
        val game = rootService.currentGame!!
        val tile = Tile(
            id = 1,
            time = 1,
            tileColour = TileColour.RED,
            tasks = listOf(),
            position = Coordinate(0,0),
            moonTrackPosition = null
        )
        game.players[game.activePlayer].tiles.add(tile)

        val expected = listOf(
            Coordinate(1,0),
            Coordinate(-1,0),
            Coordinate(0,1),
            Coordinate(0,-1)
        )
        assertEquals(expected, rootService.gameService.getPossiblePosition())
    }

    /**
     * Test that 6 unique neight positions are returned when two adjacent tiles are placed.
     */
    @Test
    fun testReturnNeighboursTwoTiles()
    {
        val game = rootService.currentGame!!

        game.players[game.activePlayer].tiles.add(
            Tile(1, 1, TileColour.RED, listOf(), Coordinate(0,0), null)
        )

        game.players[game.activePlayer].tiles.add(
            Tile(2, 1, TileColour.BLUE, listOf(), Coordinate(1,0), null)
        )

        val expectedSet = setOf(
            Coordinate(-1,0),
            Coordinate(0,1),
            Coordinate(0,-1),
            Coordinate(2,0),
            Coordinate(1,1),
            Coordinate(1,-1),
        )

        val actualSet = rootService.gameService.getPossiblePosition().toSet()
        assertEquals(expectedSet, actualSet)
    }


}