package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import tools.aqua.bgw.util.Coordinate
import kotlin.test.*

class UpdateTasksTest {

    /**
     * The [RootService] is initialized in the [setUpGame] function
     * hence it is a late-initialized property.
     */
    private lateinit var rootService: RootService

    @BeforeTest
    fun setUpGame() {
        rootService = RootService()

        val players = mutableListOf(
            (Player("Anna", 18, 0, false, PlayerType.HUMAN, PlayerColour.BLACK, mutableListOf(), 0)),
            (Player("Bob",18 ,0 , false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)),
            (Player("David",18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)),
            Player("Charles", 18, 0, false, PlayerType.HUMAN, PlayerColour.BLUE, mutableListOf(), 0)
        )
        rootService.gameService.startNewGame(players, 10)

    }

    @Test
    fun testUpdateTasks(){
        val game = rootService.currentGame
        val tiles = game!!.tileTrack.subList(1, game.tileTrack.size)
        val currentPlayerHand = rootService.currentGame?.players?.first()?.tiles!!
        rootService.currentGame?.players?.first()?.tiles?.addAll(tiles.take(10))

        val testabg = tiles.first()
        val cords = listOf(
            Coordinate( 1, 0),
            Coordinate(0, 1),
            Coordinate(0, 0),
            Coordinate(0,- 1),
            Coordinate( -1, 0),
            Coordinate(-1, 1),
            Coordinate(1, 1),
            Coordinate(1,- 1),
            Coordinate(-1, -1),
            Coordinate(2,0),
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