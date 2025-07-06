package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
/**
 * Test class for verifying the behavior of the game end logic within [GameService].
 *
 * This test ensures that:
 * - `checkEndGame()` returns null when no game is running.
 * - `endGame()` clears the game state by setting `currentGame` to null.
 */
class EndGameTest {
    /**
     * Tests the entire end game process:
     * 1. Verifies that no game is active at the beginning.
     * 2. Starts a new game with two players.
     * 3. Ends the game manually by calling `endGame()` on the first player.
     * 4. Asserts that the current game has been correctly reset to null.
     */
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
        rootService.gameService.startNewGame(players, simulationSpeed = 3, randomOrder = false, false)
        val game = rootService.currentGame!!
        rootService.gameService.endGame(game.players.first())

        assertNull(rootService.currentGame)
    }
}