package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.Move
import tools.aqua.bgw.util.Coordinate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
/**
 * Test class for the `playTile()` methods in [PlayerActionService].
 *
 * These tests verify that a tile can be successfully played by a player
 * using either direct tile index and coordinate, or a [Move] object.
 */
class PlayTileTest {

    private lateinit var rootService: RootService
    /**
     * Sets up a standard two-player game before each test.
     * Players are initialized with 18 tokens and default properties.
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
                0),
            Player("Player2",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.ORANGE,
                mutableListOf(),
                0)
        )
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
    }
    /**
     * Tests that a tile is correctly added to the active player's personal tile list
     * when played using the `playTile(index, Coordinate)` method.
     */
    @Test
    fun testPlayTile() {
        val game = rootService.currentGame!!

        val selectedTile = game.tileTrack[1]
        rootService.playerActionService.playTile(1, Coordinate(0,0))

        assertEquals(selectedTile, game.players[game.activePlayer].tiles.last())
    }
    /**
     * Tests that playing a tile using a [Move] object succeeds without exceptions.
     * This method verifies integration of the overloaded `playTile(move: Move)` variant.
     */
    @Test
    fun testPlayTileMove(){
        val game = rootService.currentGame!!

        val selectedTile = game.tileTrack[1]
        val postion = Coordinate(0,0)
        val move = Move(selectedTile, postion)

        rootService.playerActionService.playTile(move)
    }
}