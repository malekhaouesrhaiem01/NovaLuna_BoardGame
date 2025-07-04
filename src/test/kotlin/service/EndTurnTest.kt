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
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
    }
    /**
     * Verifies `endTurn()` correctly selects the next player based on moon track position
     * when no players share the same position.
     */
    @Test
    fun testNoPlayerOnSamePosition(){
        val game = rootService.currentGame!!

        game.players[0].moonTrackPosition = 20
        game.players[1].moonTrackPosition = 15
        game.players[2].moonTrackPosition = 10

        rootService.gameService.endTurn()

        assertEquals(2, game.activePlayer)
    }
    /**
     * Verifies that among players sharing the same moon track position,
     * the one with the smaller height is chosen as the next active player.
     */
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

    /**
     * Verifies that the game ends when the active player has no more tokens.
     * This simulates the winning condition being reached.
     */
    @Test
    fun testEndTurnEndsGameNoTokens(){
        val game = rootService.currentGame!!

        game.players[0].tokenCount = 7
        game.players[1].tokenCount = 10
        game.players[2].tokenCount = 0

        game.activePlayer = 2

        rootService.gameService.endTurn()

        assertEquals(null, rootService.currentGame)
    }
}