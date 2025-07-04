package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import entity.Tile
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
/**
 * Test class for the `startNewGame()` method in [GameService].
 *
 * This class verifies correct game initialization, input validation for player count and simulation speed,
 * and that starting a new game while another is active throws the correct exception.
 */
class StartNewGameTest {
    /**
     * Verifies that starting a game with invalid configurations throws appropriate exceptions,
     * and that valid initialization works only once per active game session.
     */
    @Test
    fun teststartNewGame(){
        val rootService = RootService()

        val tile:MutableList<Tile?> = mutableListOf()
        val player1 = Player("Paula", 18,0,
            false, PlayerType.HUMAN, PlayerColour.BLACK, tile, 1)
        val player2 = Player("Paula2", 18,0,
            false, PlayerType.HUMAN, PlayerColour.BLACK, tile, 1)

        assertThrows<IllegalArgumentException> { rootService.gameService.startNewGame(listOf(player1),
            4, randomOrder = false, firstGame = false
        )}
        assertThrows<IllegalArgumentException> {
            rootService.gameService.startNewGame(listOf(player1, player2),
            13, randomOrder = false, firstGame = false
            )}

        rootService.gameService.startNewGame(listOf(player1, player2),
            5, randomOrder = true, firstGame = false
        )
        assertThrows<IllegalStateException> { (rootService.gameService.startNewGame(listOf(player1, player2),
            5, randomOrder = false, firstGame = false
        )) }


    }
}