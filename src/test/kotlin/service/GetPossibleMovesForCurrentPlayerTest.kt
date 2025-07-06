package service

import entity.Move
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
/**
 * Test class for the method `getPossibleMovesForCurrentPlayer()` in [GameService].
 *
 * This class checks whether the method correctly computes all valid combinations of tile and board position
 * that the current player can play.
 */
class GetPossibleMovesForCurrentPlayerTest {

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
     * Tests that `getPossibleMovesForCurrentPlayer()` returns the correct list of [Move]s
     * by combining every available tile with every legal board coordinate.
     */
    @Test
    fun testForRightMoves(){
        val game = rootService.currentGame!!

        val expectedTiles = rootService.gameService.getAvailableTiles().map{game.tileTrack[it]}
        val expectedPositions = rootService.gameService.getPossiblePosition()

        val expectedMoves = mutableListOf<Move>()
        for(tile in expectedTiles){
            for (coord in expectedPositions){
                expectedMoves.add(Move(tile, coord))
            }
        }

        assertEquals(expectedMoves, rootService.gameService.getPossibleMovesForCurrentPlayer())
    }

}