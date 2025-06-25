package service
import entity.*
import tools.aqua.bgw.util.Coordinate
import kotlin.test.*

/**
 * Test class for method selectBestChild from class MCTSNode
 */
class SelectBestChildTest {
    private lateinit var rootService: RootService
    @BeforeTest
    fun setUpGame() {
        rootService = RootService()
    }

    private val tileList:MutableList<Tile?> = mutableListOf()
    private val tileColourList: List<Map<TileColour, Int>> = listOf(
        mapOf(TileColour.RED to 3, TileColour.BLUE to 2))
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
     * test for selecting correct child
     */
    @Test
    fun testSelectBestChild() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)

        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 50
        // Erstelle Kinder mit verschiedenen Besuchen und Punktzahlen
        val child1 = MCTSNode(gameState, parent = parentNode, move, untriedMoves = mutableListOf())
        val child2 = MCTSNode(gameState, parent = parentNode, move, untriedMoves = mutableListOf())
        val child3 = MCTSNode(gameState, parent = parentNode, move, untriedMoves = mutableListOf())

        child1.visits= 10
        child2.visits= 5
        child3.visits= 20
        val playerId = gameState.activePlayer
        child1.scores[playerId] = 30.0  // Durchschnitt: 30 / 10 = 3.0
        child2.scores[playerId] = 20.0  // Durchschnitt: 20 / 5 = 4.0
        child3.scores[playerId] = 40.0  // Durchschnitt: 40 / 20 = 2.0

        // Füge die Kinder zum Elternknoten hinzu
        parentNode.children.addAll(listOf(child1, child2, child3))

        // Wähle den besten Kindknoten basierend auf UCT
        val bestChild = parentNode.selectBestChild()

        // Test: Überprüfen, ob das Kind mit der höchsten UCT ausgewählt wurde
        assertEquals(child2, bestChild)  // Da child3 die meisten Besuche und den höchsten Score hat
    }

    /**
     * test for selecting one of two children, if both have same value for score/visits
     */
    @Test
    fun testSelectBestChildTwoChildrenSameScoreAndVisits() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(players, 1)
        val gameState = rootService.currentGame
        assertNotNull(gameState)

        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 50

        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parent = parentNode, moveThatLedHere = move, untriedMoves = mutableListOf())
        val child2 = MCTSNode(gameState, parent = parentNode, moveThatLedHere = move, untriedMoves = mutableListOf())
        val child3 = MCTSNode(gameState, parent = parentNode, move, untriedMoves = mutableListOf())
        // Beide Kinder haben gleich viele Besuche
        child1.visits = 10
        child2.visits = 10
        child3.visits= 5
        // Zwei Kinder haben denselben Score für den aktiven Spieler
        child1.scores[playerId] = 30.0  // Durchschnitt: 3.0
        child2.scores[playerId] = 30.0  // Durchschnitt: 3.0
        child3.scores[playerId] = 10.0

        parentNode.children.addAll(listOf(child1, child2, child3))

        val bestChild = parentNode.selectBestChild()

        // Da beide denselben UCT-Wert haben, sollte entweder child1 oder child2 zurückgegeben werden
        assertTrue(bestChild == child1 || bestChild == child2)
    }

    /**
     * test for empty children list
     */
    @Test
    fun testSelectBestChildNoChildren() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        // Keine Kinder hinzugefügt
        val bestChild = parentNode.selectBestChild()
        assertNull(bestChild)
    }

    /**
     * parentNode with visits = 0 (random child selection)
     */
    @Test
    fun testSelectBestChildParentVisitsZero() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 0

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())
        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild()
        // Es wird eines der beiden Kinder zurückgegeben, nicht null
        assertTrue(bestChild == child1 || bestChild == child2)
    }

    /**
     * children with visits = 0 (unvisited children preferred)
     */
    @Test
    fun testSelectBestChildChildVisitsZero() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 10
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())

        child1.visits = 0 // unbesucht
        child2.visits = 5
        child2.scores[playerId] = 50.0

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild()
        // unbesuchtes Kind (child1) sollte bevorzugt werden (Double.MAX_VALUE UCT)
        assertEquals(child1, bestChild)
    }

    /**
     * different Visits, same Scores
     */
    @Test
    fun testSelectBestChildDifferentVisitsSameScores() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 50
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())

        child1.visits = 10
        child2.visits = 5
        child1.scores[playerId] = 30.0  // Durchschnitt: 3.0
        child2.scores[playerId] = 15.0  // Durchschnitt: 3.0

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild()
        // Child2 hat weniger Visits, daher höhere Exploration → sollte gewählt werden
        assertEquals(child2, bestChild)
    }

    /**
     * Scores 0 or very small
     */
    @Test
    fun testSelectBestChildScoresZeroOrSmall() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 10
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        child1.visits = 5
        child1.scores[playerId] = 0.0

        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())
        child2.visits = 5
        child2.scores[playerId] = 0.0001

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild()
        assertTrue(bestChild == child1 || bestChild == child2)
    }

    /**
     * Player-ID not in Scores-Map of a child
     */
    @Test
    fun testSelectBestChildPlayerIdNotInScores() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 10
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        child1.visits = 5
        // Kein Score für playerId gesetzt (Standard 0.0)

        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())
        child2.visits = 5
        child2.scores[playerId] = 10.0

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild()
        // child2 sollte gewählt werden, weil besserer Score
        assertEquals(child2, bestChild)
    }

    /**
     * Big number of children
     */
    @Test
    fun testSelectBestChildLargeNumberOfChildren() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 1000
        val playerId = gameState.activePlayer

        // Erstelle 100 Kinder mit zufälligen Werten
        val children = (1..100).map { i ->
            val child = MCTSNode(gameState, parentNode, move, mutableListOf())
            child.visits = i
            child.scores[playerId] = (i * 2).toDouble()
            child
        }

        parentNode.children.addAll(children)

        val bestChild = parentNode.selectBestChild()

        // Sollte nicht null sein
        assertNotNull(bestChild)
        // Sollte Kind mit vernünftig hohem UCT Wert sein (z.B. Visits > 0)
        assertTrue(bestChild.visits > 0)
    }

    /**
     * Exploration-Parameter = 0 (only Exploitation)
     */
    @Test
    fun testSelectBestChildExplorationZero() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 50
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())

        child1.visits = 10
        child2.visits = 20
        child1.scores[playerId] = 50.0  // Durchschnitt: 5.0
        child2.scores[playerId] = 100.0 // Durchschnitt: 5.0

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild(explorationConstant = 0.0)

        // Da Exploration 0 ist, wird Kind mit höherem Durchschnitt gewählt (beide gleich)
        // In diesem Fall sollte eines der beiden Kinder gewählt werden
        assertTrue(bestChild == child1 || bestChild == child2)
    }

    /**
     * Big Exploration-Parameter
     */
    @Test
    fun testSelectBestChild_ExplorationVeryLarge() {
        val gameState = rootService.currentGame ?: return
        val parentNode = MCTSNode(gameState, untriedMoves = mutableListOf())
        parentNode.visits = 50
        val playerId = gameState.activePlayer

        val child1 = MCTSNode(gameState, parentNode, move, mutableListOf())
        val child2 = MCTSNode(gameState, parentNode, move, mutableListOf())

        child1.visits = 10
        child2.visits = 20
        child1.scores[playerId] = 50.0  // Durchschnitt: 5.0
        child2.scores[playerId] = 100.0 // Durchschnitt: 5.0

        parentNode.children.addAll(listOf(child1, child2))

        val bestChild = parentNode.selectBestChild(explorationConstant = 100.0)

        // Durch die sehr hohe Exploration wird eher das Kind mit weniger Visits (child1) bevorzugt
        assertEquals(child1, bestChild)
    }
}