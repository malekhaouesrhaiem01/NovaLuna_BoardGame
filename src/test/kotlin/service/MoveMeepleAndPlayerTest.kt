package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.*

class MoveMeepleAndPlayerTest {
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
    fun testMoveMeepleAndPlayer(){
        val game = rootService.currentGame!!

        val tile = game.tileTrack[2]
        checkNotNull(tile)
        tile.moonTrackPosition = 2
        val time = tile.time
        val playerMoonTrackPos = game.players[game.activePlayer].moonTrackPosition


        rootService.gameService.moveMeepleAndPlayer(tile)

        assert(game.tileTrack[2] == null)
        assert(tile.moonTrackPosition == null)
        assert(game.players[game.activePlayer].moonTrackPosition == playerMoonTrackPos + time)

    }
}