package service

import entity.Move
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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