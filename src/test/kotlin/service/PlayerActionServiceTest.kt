package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.SerializableCoordinate
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest

/**
 * Test the functionality for the functions undo(), redo(), load() and save()
 * in the Class PlayerActionService
 */
class PlayerActionServiceTest {
    /**
     * rootService to access the PlayerActionServices
     */
    private lateinit var rootService: RootService
    /**
     * The Set-Up before every Test is started
     */
    @BeforeTest
    fun setUp() {
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
     * Tests the undo function
     */
    @Test
    fun undoTest(){
        rootService.playerActionService.playTile(tileTrackIndex = 2, SerializableCoordinate(0.0,0.0))
        rootService.playerActionService.undo()

        //assertEquals(game , rootService.currentGame)

    }

    /**
     * Tests the redo function
     */
    @Test
    fun redoTest(){
        rootService.playerActionService.playTile(tileTrackIndex = 2, SerializableCoordinate(0.0,0.0))
        rootService.playerActionService.undo()
        rootService.playerActionService.redo()

        //assertNotEquals(game, rootService.currentGame)
    }

    /**
     * Tests the save function
     */
    @Test
    fun saveTest(){
        rootService.playerActionService.playTile(tileTrackIndex = 2, SerializableCoordinate(0.0,0.0))
        rootService.playerActionService.save()

    }

    /**
     * Tests the load function
     */
    @Test
    fun loadTest(){
        rootService.playerActionService.load()
        rootService.gameService.endGame(rootService.currentGame!!.players[1])
        //rootService.playerActionService.playTile(tileTrackIndex = 2, SerializableCoordinate(0.0,0.0))
        rootService.playerActionService.load()
    }
}