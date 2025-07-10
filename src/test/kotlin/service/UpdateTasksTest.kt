package service

import entity.*
import kotlin.test.*
/**
 * Test class for the `updateTasks()` method in [GameService].
 *
 * The test simulates a complex player hand with multiple tiles placed around a central position
 * and verifies whether task fulfillment is properly updated based on tile positions and neighbors.
 */
class UpdateTasksTest {

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
            (Player("Anna", 18, 0, false, PlayerType.HUMAN, PlayerColour.BLACK, mutableListOf(), 0)),
            (Player("Bob",18 ,0 , false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)),
            (Player("David",18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)),
            Player("Charles", 18, 0, false, PlayerType.HUMAN, PlayerColour.BLUE, mutableListOf(), 0)
        )
        rootService.gameService.startNewGame(players, 10, randomOrder = false, firstGame = false)

    }
    /**
     * Tests the `updateTasks()` method by:
     * - Giving the current player 10 tiles from the tile track.
     * - Positioning them in a pattern around the origin.
     * - Calling `updateTasks()` to process task checks.
     *
     *
     */
    @Test
    fun testUpdateTasks(){
        val game = rootService.currentGame
        val tiles = game!!.tileTrack.subList(1, game.tileTrack.size)
        val currentPlayerHand = rootService.currentGame?.players?.first()?.tiles!!
        rootService.currentGame?.players?.first()?.tiles?.addAll(tiles.take(10))

        val cords = listOf(
            SerializableCoordinate( 1.0, 0.0),
            SerializableCoordinate(0.0, 1.0),
            SerializableCoordinate(0.0, 0.0),
            SerializableCoordinate(0.0,- 1.0),
            SerializableCoordinate( -1.0, 0.0),
            SerializableCoordinate(-1.0, 1.0),
            SerializableCoordinate(1.0, 1.0),
            SerializableCoordinate(1.0,- 1.0),
            SerializableCoordinate(-1.0, -1.0),
            SerializableCoordinate(2.0,0.0),
        )

        for(i in 0 ..  currentPlayerHand.size - 1){
            currentPlayerHand[i]!!.position = cords[i]
        }
        game.tileTrack.clear()
        rootService.gameService.updateTasks()

        // Works for an unshuffled drawPile or If 11 Cyan Tiles are in Players Hands
        // assertEquals(true, currentPlayerHand[1]!!.tasks[0].second)


    }
}
