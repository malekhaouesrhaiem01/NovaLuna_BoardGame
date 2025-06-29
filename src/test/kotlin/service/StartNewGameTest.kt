package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.Tile
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class StartNewGameTest {

    @Test
    fun teststartNewGame(){
        val rootService = RootService()

        val tile:MutableList<Tile?> = mutableListOf()
        val player1 = Player("Paula", 18,0,
            false, PlayerType.HUMAN, PlayerColour.BLACK, tile, 1)
        val player2 = Player("Paula2", 18,0,
            false, PlayerType.HUMAN, PlayerColour.BLACK, tile, 1)

        assertThrows<IllegalArgumentException> { rootService.gameService.startNewGame(listOf(player1),
            4, false)}
        assertThrows<IllegalArgumentException> {
            rootService.gameService.startNewGame(listOf(player1, player2),
            13, false)}

        rootService.gameService.startNewGame(listOf(player1, player2),
            5, true)
        assertThrows<IllegalStateException> { (rootService.gameService.startNewGame(listOf(player1, player2),
            5, false)) }


    }
}