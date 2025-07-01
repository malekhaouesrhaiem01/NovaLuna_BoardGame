package service
import entity.*
import service.bot.MCTSNode
import kotlin.test.*

/**
 * Test class for method isTerminal from class MCTSNode
 */
class IsTerminalTest {
    private lateinit var rootService: RootService
    @BeforeTest
    fun setUpGame() {
        rootService = RootService()
    }

    private val tile:MutableList<Tile?> = mutableListOf()
    private val player1 = Player("Alice", 18,0,
        false, PlayerType.HUMAN, PlayerColour.BLACK, tile, 1)
    private val player2 = Player("Bob", 18,0,
        false, PlayerType.HUMAN, PlayerColour.WHITE, tile, 0)

    private val player3 = Player("Dennis", 18,0,
        false, PlayerType.HUMAN, PlayerColour.BLUE, tile, 2)
    private val player4 = Player("Hannah", 18,0,
        false, PlayerType.HUMAN, PlayerColour.ORANGE, tile, 3)
    private val players = mutableListOf(player1, player2, player3, player4)

    /**
     * Test for isTerminal() with no tokens for one player
     */
    @Test
    fun testIsTerminalToken() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)
        // Test: Wenn alle Tokens eines Spielers verbraucht sind
        assertNotNull(gameState.tileTrack)
        assertNotNull(gameState.drawPile)
        assertNotNull(gameState.players)
        gameState.players[0].tokenCount = 0

        val nodeWithEmptyTokens = MCTSNode(gameState, untriedMoves = mutableListOf())
        assertTrue(nodeWithEmptyTokens.isTerminal())
    }

    /**
     * Test for isTerminal() with empty drawPile and empty tileTrack
     */
    @Test
    fun testIsTerminalPile() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)
        assertNotNull(gameState.players)
        // Test wenn drawPile und tileTrack leer sind
        gameState.tileTrack.clear()
        gameState.drawPile.clear()
        assertTrue(gameState.players[0].tokenCount >0)
        assertTrue(gameState.players[1].tokenCount >0)
        assertTrue(gameState.players[2].tokenCount >0)
        assertTrue(gameState.players[3].tokenCount >0)
        val nodeWithEmptyPiles = MCTSNode(gameState, untriedMoves = mutableListOf())
        assertTrue(nodeWithEmptyPiles.isTerminal())

    }

    /**
     * Test for isTerminal() returning false if all players have tokens
     * and drawPile and tileTrack are not empty
     */
    @Test
    fun testIsTerminalFalse() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)
        assertNotNull(gameState.players)
        assertNotNull(gameState.tileTrack)
        assertNotNull(gameState.drawPile)
        assertTrue(gameState.players[0].tokenCount >0)
        assertTrue(gameState.players[1].tokenCount >0)
        assertTrue(gameState.players[2].tokenCount >0)
        assertTrue(gameState.players[3].tokenCount >0)
        assertTrue(gameState.tileTrack.isNotEmpty() && gameState.drawPile.isNotEmpty())
        // Test: Wenn das Spiel noch nicht beendet ist
        val nodeNotTerminal = MCTSNode(gameState, untriedMoves = mutableListOf())
        assertFalse(nodeNotTerminal.isTerminal())
    }
}