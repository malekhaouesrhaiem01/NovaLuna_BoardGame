package service

import entity.*
import service.bot.EasyBotService
import kotlin.test.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test class for ExecuteEasyMoveTest from EasyBotService
 */
class ExecuteEasyMoveTest {
    private lateinit var rootService: RootService
    private val tileList: MutableList<Tile?> = mutableListOf()
    private val tileColourList: List<Pair<Map<TileColour, Int>, Boolean>> = listOf(Pair(
        mapOf(TileColour.RED to 3, TileColour.BLUE to 2), false))

    private val pos: SerializableCoordinate = SerializableCoordinate(0.0, 0.0)
    private val easyBotPlayer = Player(
        "EasyBot",
        18, 0,
        false,
        PlayerType.EASYBOT,
        PlayerColour.BLACK,
        tileList,
        0
    )
    private val humanPlayer = Player(
        "Human",
        18, 0,
        false,
        PlayerType.HUMAN,
        PlayerColour.WHITE,
        tileList,
        1
    )
    private val tile = Tile(1, 2, TileColour.BLUE, tileColourList, null, 1)
    private val move = Move(tile, pos)
    /**
     * Initializes predefined settings before each test.
     */
    @BeforeTest
    fun setUpGame() {
        rootService = RootService()
    }
    /**
     * test for correct function of wait time and bot plays a move
     */
    @Test
    fun testExecuteEasyMoveRunsAndPlaysMove() {
        assertNull(rootService.currentGame)
        // Spiel mit EASYBOT starten
        rootService.gameService.startNewGame(mutableListOf(easyBotPlayer, humanPlayer), 1,
            randomOrder = false,
            firstGame = false
        )
        val game = rootService.currentGame
        assertNotNull(game)
        game.activePlayer = 0 // EASYBOT am Zug
        game.simulationSpeed = 1 // 1 Sekunde warten

        // Brauchen einen Scheduler und den Bot
        val bot = EasyBotService(rootService)

        // Latch für Synchronisation (warten, bis playTile aufgerufen wird)
        val latch = CountDownLatch(1)

        // Dummy playerActionService mit Hook, um Latch zu triggern
        rootService.playerActionService = object : PlayerActionService(rootService) {
            override fun playTile(move: Move) {
                latch.countDown()
                println("playTile wurde aufgerufen mit: $move")
            }
        }
        // Dummy gameService mit möglichen Zügen
        rootService.gameService = object : GameService(rootService) {
            override fun getPossibleMovesForCurrentPlayer(): List<Move> {
                return listOf(move)
            }
        }
        bot.executeEasyMove()
        val success = latch.await(2, TimeUnit.SECONDS)

        assertTrue(success, "EasyBot hat den Zug innerhalb der Wartezeit gespielt")
    }

    /**
     * test that trying to play a tile with no possible moves fails
     */
    @Test
    fun testExecuteEasyMoveThrowsIfNoPossibleMoves() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(mutableListOf(easyBotPlayer, humanPlayer), 1,
            randomOrder = false,
            firstGame = false
        )
        val game = rootService.currentGame
        assertNotNull(game)
        game.activePlayer = 0 // EASYBOT am Zug
        game.simulationSpeed = 1
        val bot = EasyBotService(rootService)
        // GameService gibt keine möglichen Züge zurück
        rootService.gameService = object : GameService(rootService) {
            override fun getPossibleMovesForCurrentPlayer(): List<Move> = emptyList()
        }
        val exception = assertFailsWith<IllegalStateException> {
            bot.executeEasyMove()
        }
        assert(exception.message?.contains("no moves are available") == true)
    }

    /**
     * test for current player is not an easyBot fails
     */
    @Test
    fun testExecuteEasyMoveThrowsIfCurrentPlayerIsNotEasyBot() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(mutableListOf(humanPlayer, easyBotPlayer), 1,
            randomOrder = false,
            firstGame = false
        )
        val game = rootService.currentGame
        assertNotNull(game)
        game.activePlayer = 0 // Human am Zug (kein EASYBOT)
        game.simulationSpeed = 1
        val bot = EasyBotService(rootService)
        val exception = assertFailsWith<IllegalStateException> {
            bot.executeEasyMove()
        }
        assert(exception.message?.contains("not an EASYBOT") == true)
    }

    /**
     * test for different simulation speeds
     */
    @Test
    fun testExecuteEasyMoveWithDifferentSimulationSpeeds() {
        assertNull(rootService.currentGame)
        rootService.gameService.startNewGame(mutableListOf(easyBotPlayer, humanPlayer), 1,
            randomOrder = false,
            firstGame = false
        )
        val game = rootService.currentGame
        assertNotNull(game)
        game.activePlayer = 0 // EASYBOT am Zug
        val bot = EasyBotService(rootService)
        val speeds = listOf(0, 1, 3) // 0 Sekunden, 1 Sekunde, 3 Sekunden
        for (speed in speeds) {
            game.simulationSpeed = speed
            val latch = CountDownLatch(1)
            rootService.playerActionService = object : PlayerActionService(rootService) {
                override fun playTile(move: Move) {
                    latch.countDown()
                }
            }
            rootService.gameService = object : GameService(rootService) {
                override fun getPossibleMovesForCurrentPlayer(): List<Move> = listOf(move)
            }
            bot.executeEasyMove()
            val waited = latch.await((speed + 1).toLong(), TimeUnit.SECONDS)
            assertTrue(waited, "EasyBot hat bei speed=$speed den Zug ausgeführt")
        }
    }
}
