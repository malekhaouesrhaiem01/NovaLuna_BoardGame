package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.Move
import tools.aqua.bgw.util.Coordinate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayTileTest {

    private lateinit var rootService: RootService

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
                0)
        )
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
    }

    @Test
    fun testPlayTile() {
        val game = rootService.currentGame!!

        val selectedTile = game.tileTrack[1]
        rootService.playerActionService.playTile(1, Coordinate(0,0))

        assertEquals(selectedTile, game.players[game.activePlayer].tiles.last())
    }

    @Test
    fun testPlayTileMove(){
        val game = rootService.currentGame!!

        val selectedTile = game.tileTrack[1]
        val postion = Coordinate(0,0)
        val move = Move(selectedTile, postion)

        rootService.playerActionService.playTile(move)
    }
}