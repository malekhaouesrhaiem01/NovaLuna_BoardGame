package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for method endTurn() from class GameService.kt
 */
class EndTurnTest {

    private lateinit var  rootService: RootService

    /**
     * setup for a three player Game before each test
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
                0),
            Player("Player3",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.BLUE,
                mutableListOf(),
                0)
        )
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false)
    }

    @Test
    fun testNoPlayerOnSamePosition(){
        val game = rootService.currentGame!!

        game.players[0].moonTrackPosition = 20
        game.players[1].moonTrackPosition = 15
        game.players[2].moonTrackPosition = 10

        rootService.gameService.endTurn()

        assertEquals(2, game.activePlayer)
    }

    @Test
    fun testPlayerOnSamePosition(){
        val game = rootService.currentGame!!

        game.players[0].moonTrackPosition = 20
        game.players[1].moonTrackPosition = 15
        game.players[2].moonTrackPosition = 15

        game.players[1].height = 0
        game.players[2].height = 1

        rootService.gameService.endTurn()

        assertEquals(2, game.activePlayer)
    }
}