package service
import entity.*
import service.bot.MCTSNode
import tools.aqua.bgw.util.Coordinate
import kotlin.test.*

/**
 * Test class for method isFullyExpanded from class MCTSNode
 */
class IsFullyExpandedTest {
    private lateinit var rootService: RootService
    @BeforeTest
    fun setUpGame() {
        rootService = RootService()
    }

    private val tileList:MutableList<Tile?> = mutableListOf()
    private val tileColourList: List<Pair<Map<TileColour, Int>, Boolean>> = listOf(Pair(
        mapOf(TileColour.RED to 3, TileColour.BLUE to 2), false))
    private val pos: Coordinate = Coordinate(0,0)
    private val player1 = Player("Alice", 18,0,
        false, PlayerType.HUMAN, PlayerColour.BLACK, tileList, 1)
    private val player2 = Player("Bob", 18,0,
        false, PlayerType.HUMAN, PlayerColour.WHITE, tileList, 0)

    private val player3 = Player("Dennis", 18,0,
        false, PlayerType.HUMAN, PlayerColour.BLUE, tileList, 2)
    private val player4 = Player("Hannah", 18,0,
        false, PlayerType.HUMAN, PlayerColour.ORANGE, tileList, 3)
    private val players = mutableListOf(player1, player2, player3, player4)
    private val tile = Tile(1, 2, TileColour.BLUE, tileColourList, null,
        1)
    private val move= Move(tile, pos)

    /**
     * Test for node is fully expanded - no untried moves
     */
    @Test
    fun testIsFullyExpandedTrue() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)

        // Test: Wenn untriedMoves leer sind
        val nodeWithNoMoves = MCTSNode(gameState, untriedMoves = mutableListOf())
        assertTrue(nodeWithNoMoves.isFullyExpanded())
    }

    /**
     * Test for node is not fully expanded - untried moves not empty
     */
    @Test
    fun testIsFullyExpandedFalse() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)

        // Test: Wenn es noch untriedMoves gibt
        val nodeWithMoves = MCTSNode(gameState, untriedMoves = mutableListOf(move))
        assertFalse(nodeWithMoves.isFullyExpanded())
    }
}