package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.*
/**
 * Test class for the `startTurn()` method in [GameService].
 *
 * It verifies proper turn initialization, including handling of tile selection and automatic
 * refilling of the moon wheel (`tileTrack`) when necessary.
 */
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
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
    }
    /**
     * Verifies that calling `startTurn()` sets up the turn correctly,
     * and the game still contains selectable tiles (i.e., not all null).
     */
    @Test
    fun testStartTurn(){
        val game = rootService.currentGame!!

        rootService.gameService.startTurn()

        assert(game.tileTrack.contains(null))

    }
    /**
     * Verifies that if the moon wheel is empty (all null), calling `startTurn()` automatically refills it.
     */
    @Test
    fun testAutoRefill() {
        val game = rootService.currentGame!!

        game.tileTrack.fill(null)

        rootService.gameService.startTurn()

        assertEquals(11, game.tileTrack.count { it != null })
    }
}