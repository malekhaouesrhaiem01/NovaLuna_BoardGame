package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import tools.aqua.bgw.util.Coordinate
import kotlin.test.*

class StartTurnTest {
    private lateinit var  rootService: RootService

    /**
     * setup for a two player Game before each test
     */
    @BeforeTest
    fun setUp()
    {

        rootService = RootService()
        val players = listOf(
            Player(
                "Player1",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                1
            ),
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

    @Test
    fun testStartTurn(){
        val game = rootService.currentGame!!

        rootService.gameService.startTurn()

        assert(game.tileTrack.contains(null))

    }

    @Test
    fun testAutoRefill() {
        val game = rootService.currentGame!!

        game.tileTrack.fill(null)

        rootService.gameService.startTurn()

        assertEquals(11, game.tileTrack.count { it != null })
    }
}