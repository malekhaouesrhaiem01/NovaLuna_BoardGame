package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class EndGameTest {

    @Test
    fun testEndGame(){
        val rootService = RootService()
        assertNull(rootService.gameService.checkEndGame())

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
        val game = rootService.currentGame!!
        rootService.gameService.endGame(game.players.first())

        assertNull(rootService.currentGame)
    }
}