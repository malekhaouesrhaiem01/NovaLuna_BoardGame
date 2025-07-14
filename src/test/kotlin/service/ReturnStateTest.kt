package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Tests the ReturnState functionality
 */
class ReturnStateTest {

    /**
     * The [RootService] is initialized in the [setUpGame] function
     * hence it is a late-initialized property.
     */
    private lateinit var rootService: RootService
    /**
     * Initializes a 4-player game with predefined settings before each test.
     */
    @BeforeTest
    fun setUpGame() {
        rootService = RootService()

        val players = mutableListOf(
            (Player("Anna", 18, 0, false, PlayerType.EASYBOT, PlayerColour.BLACK, mutableListOf(), 0)),
            (Player("Bob",18 ,0 , false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)),
            (Player("David",18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)),
            Player("Charles", 18, 0, false, PlayerType.HUMAN, PlayerColour.BLUE, mutableListOf(), 0)
        )
        rootService.gameService.startNewGame(players, 10, randomOrder = false, firstGame = false)

    }

    /**
     * Tests the returnTurnState function
     */
    @Test
    fun testReturnState(){
        val game = rootService.currentGame

        rootService.gameService.restoreTurnState()
    }
}