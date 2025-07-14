package service
import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage
import edu.udo.cs.sopra.ntf.subtypes.Color
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import org.junit.jupiter.api.assertThrows
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import kotlin.test.assertFails



/**
 * Test class for the NetworkService Class
 */
class NetworkServiceTest {

    private val secret = "neumond25"


    private val p1 = Player("Paula", 0, 0, false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)
    private val p2 = Player("Lilly", 0, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)
    private val p3 = Player("Zenon", 0, 0, false, PlayerType.HUMAN, PlayerColour.BLACK, mutableListOf(), 0)
    private val p4 = p1.copy(playerName = "Xanthe")
    private val p5 = p1.copy(playerName = "Quentin")
    /**
     * Tests Hosting a Game with no Name Input
     */
    @Test
    fun connectBlanks() {
        val svc = NetworkService(RootService())
        assertFailsWith<IllegalArgumentException> { svc.hostGame("", "Paula") }
        assertFailsWith<IllegalArgumentException> { svc.hostGame("starlight42", "") }
    }

    /**
     * Tests connection when histing a Game
     */
    @Test
    fun connectSuccess() {
        val svc = NetworkService(RootService())
        svc.disconnect()
        svc.hostGame(secret, "Paula")
        assertEquals(
            ConnectionState.WAITING_FOR_HOST_CONFIRMATION,
            svc.connectionState
        )
    }

    /**
     * Tests if connecting twice throws an Exception
     */
    @Test
    fun connectAlready() {
        val svc = NetworkService(RootService())
        svc.disconnect()
        svc.hostGame(secret, "Lilly")
        assertFailsWith< IllegalArgumentException> {
            svc.hostGame(secret, "Zenon")
        }
    }

    /**
     * Tests Hosting a Game with the wrong state throws an Exception
     */
    @Test
    fun startHostedWrongState() {
        val svc = NetworkService(RootService())
        assertFailsWith<IllegalStateException> {
            svc.startNewHostedGame(listOf(p1, p2), isFirstGame = false, randomOrder = true)
        }
    }

    /**
     * Tests hosting a Game with wrong number of Players
     */
    @Test
    fun startHostedBadSizes() {
        val svc = NetworkService(RootService())
        svc.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)

        assertFailsWith<IllegalArgumentException> {
            svc.startNewHostedGame(listOf(p1), isFirstGame = false, randomOrder = false)
        }
        assertFailsWith<IllegalArgumentException> {
            svc.startNewHostedGame(listOf(p1, p2, p3, p4, p5), isFirstGame = true, randomOrder = true)
        }
    }

    /**
     * Tests sending the Wrong turn Message
     */
    @Test
    fun sendTurnWrongState() {
        val svc = NetworkService(RootService())
        // default state is DISCONNECTED
        assertFailsWith<IllegalStateException> {
            svc.sendTurnMessage(tileId = 42, x = 1, y = 2, refillTrack = false)
        }
    }
    /**
     * Tests for the HostGame function
     */
    @Test
    fun testHostGame() {
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        assertThrows<IllegalStateException> { networkService.hostGame("altesonne24", "Paula") }
        assertThrows<IllegalArgumentException> { networkService.hostGame("", "Paula") }
        assertThrows<IllegalArgumentException> { networkService.hostGame("neumond25", "") }
        networkService.hostGame("neumond25", "Paula")
        assertThrows<IllegalArgumentException> { networkService.hostGame("neumond", "Paula", "Session1") }
        networkService.disconnect()
        networkService.hostGame("neumond25", "Paula", "Session1")
        assertEquals(ConnectionState.WAITING_FOR_HOST_CONFIRMATION, networkService.connectionState)


    }

    /**
     * Test for the JoinGame function
     */
    @Test
    fun testJoinGame() {
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        assertThrows<IllegalStateException> {
            networkService.joinGame(
                "altesonne24", "Paula",
                "Session1"
            )
        }

        networkService.joinGame("neumond25", "Paula", "Session1")
        assertEquals(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION, networkService.connectionState)
    }

    /**
     * Test for the StartNewHostedGame function
     */
    @Test
    fun testStartNewHostedGame() {
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        val players = listOf(
            Player("Paula", 18, 0, false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0),
            Player("Player2", 18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)
        )

        assertThrows<IllegalStateException> {
            networkService.startNewHostedGame(players, isFirstGame = false, randomOrder = true)
        }

        networkService.hostGame("neumond25", "Paula", "Session1")
        assertEquals(ConnectionState.WAITING_FOR_HOST_CONFIRMATION, networkService.connectionState)

        networkService.onCreateGameResponse(
            CreateGameResponse(CreateGameResponseStatus.SUCCESS to "Session1"), "Paula")
        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)

        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)

        assertThrows<IllegalArgumentException> {
            networkService.startNewHostedGame(
                listOf(players[0]),
                isFirstGame = false,
                randomOrder = true
            )
        }

        networkService.startNewHostedGame(players, isFirstGame = false, randomOrder = true)

        val players2 = listOf(
            Player("Player1", 18, 0, false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0),
            Player("Paula",   18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)
        )

        rootService.currentGame = null
        networkService.disconnect()

        networkService.hostGame("neumond25", "Paula", "Session2")
        networkService.onCreateGameResponse(
            CreateGameResponse(CreateGameResponseStatus.SUCCESS to "Session2"), "Paula")
        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        networkService.startNewHostedGame(players2, isFirstGame = false, randomOrder = true)
    }

    /**
     * Test for the StartNewJoinGame function
     */
    @Test
    fun testStartNewJoinedGame(){
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        val players = listOf(
            edu.udo.cs.sopra.ntf.subtypes.Player("Paula", Color.BLUE),
            edu.udo.cs.sopra.ntf.subtypes.Player("Lilly", Color.BLACK)
        )
        val drawPileIDs = (1..68).shuffled()
        val message = InitMessage(drawPileIDs, false, players)
        val joinGameResponse = JoinGameResponse(JoinGameResponseStatus.SUCCESS,
            "Session1", message = "Hi", opponents = listOf("Paula", "Lilly"))

        assertThrows<IllegalStateException> { networkService.startNewJoinedGame(message) }

        networkService.joinGame("neumond25", "Paula", "Session1")
        networkService.onJoinGameResponse(joinGameResponse, "Lotte")
        networkService.startNewJoinedGame(message)


        val players2 = listOf(
            edu.udo.cs.sopra.ntf.subtypes.Player("Lilly", Color.BLUE),
            edu.udo.cs.sopra.ntf.subtypes.Player("Paula", Color.BLACK)
        )
        rootService.currentGame = null
        networkService.disconnect()

        val message2 = InitMessage(drawPileIDs, false, players2)
        val joinGameResponse2 = JoinGameResponse(JoinGameResponseStatus.SUCCESS,
            "Session1", message = "Hi", opponents = listOf("Paula", "Lilly"))

        assertThrows<IllegalStateException> { networkService.startNewJoinedGame(message) }

        networkService.joinGame("neumond25", "Paula", "Session1")
        networkService.onJoinGameResponse(joinGameResponse2, "Lotte")
        networkService.startNewJoinedGame(message2)
    }

    /**
     * Tests for the ReceiveTurnMessage function
     */
    @Test
    fun testReceiveTurnMessage(){

        val rootService = RootService()
        val networkService = NetworkService(rootService)

        val players1 = listOf(
            Player("Player1", 18, 0, false, PlayerType.HUMAN, PlayerColour.WHITE,  mutableListOf(), 0),
            Player("Paula",   18, 0, false, PlayerType.HUMAN, PlayerColour.ORANGE, mutableListOf(), 0)
        )

        networkService.hostGame("neumond25", "Paula", "Session2")
        networkService.onCreateGameResponse(
            CreateGameResponse(CreateGameResponseStatus.SUCCESS to "Session2"),
            "Paula"
        )
        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        networkService.startNewHostedGame(players1, isFirstGame = false, randomOrder = false)

        networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        val tileTrack = rootService.currentGame!!.tileTrack
        val message = TurnMessage(tileTrack[1]!!.id, 0, 0, refillTrack = false)
        networkService.receiveTurnMessage(message, "Player1")
    }

    /**
     * startNewHostedGame must reject when not in the lobby state
     */
    @Test
    fun startNewHostedGame_wrongState() {
        val svc = NetworkService(RootService())
        assertFailsWith<IllegalStateException> {
            svc.startNewHostedGame(emptyList(), isFirstGame = false, randomOrder = false)
        }
    }

    /**
     * startNewHostedGame must reject when list size < 2
     */

    @Test
    fun startNewHostedGame_tooFewPlayers() {
        val svc = NetworkService(RootService())
        // Pretend we’re already in the lobby
        svc.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)

        val onePlayer = listOf(
            Player("Paula", 18, 0, false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)
        )
        assertFailsWith<IllegalArgumentException> {
            svc.startNewHostedGame(onePlayer, isFirstGame = false, randomOrder = false)
        }
    }

    /**
     * StartNewHostedGame must reject when list size bigger than 4
     */
    @Test
    fun startNewHostedGame_tooManyPlayers() {
        val svc = NetworkService(RootService())
        svc.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)

        val base = Player("Paula", 18, 0, false, PlayerType.HUMAN, PlayerColour.WHITE, mutableListOf(), 0)
        val players5 = listOf(
            base,
            base.copy(playerName = "Lilly"),
            base.copy(playerName = "Zenon"),
            base.copy(playerName = "Xanthe"),
            base.copy(playerName = "Quentin")
        )
        assertFailsWith<IllegalArgumentException> {
            svc.startNewHostedGame(players5, isFirstGame = true, randomOrder = true)
        }
    }

    /**
     * sendTurnMessage must reject when not in PLAYING_MY_TURN
     */
    @Test
    fun sendTurnMessage_wrongState() {
        val svc = NetworkService(RootService())
        assertFailsWith<IllegalStateException> {
            svc.sendTurnMessage(tileId = 42, x = 1, y = 1, refillTrack = false)
        }
    }

    /**
     * receiveTurnMessage must reject when not in WAITING_FOR_OPPONENT
     */
    @Test
    fun receiveTurnMessage_wrongState() {
        val svc = NetworkService(RootService())
        val dummy = TurnMessage(tileId = 99, x = 0, y = 0, refillTrack = false)
        assertFailsWith<IllegalStateException> {
            svc.receiveTurnMessage(dummy, sender = "Paula")
        }
    }

    /**
     * startNewJoinedGame must reject when not in WAITING_FOR_INIT
     */
    @Test
    fun startNewJoinedGame_wrongState() {
        val svc = NetworkService(RootService())
        val dtoList = listOf(
            edu.udo.cs.sopra.ntf.subtypes.Player("Paula", edu.udo.cs.sopra.ntf.subtypes.Color.BLUE),
            edu.udo.cs.sopra.ntf.subtypes.Player("Lilly", edu.udo.cs.sopra.ntf.subtypes.Color.BLACK)
        )
        val msg = InitMessage(drawPile = emptyList(), isFirstGame = true, players = dtoList)
        assertFailsWith<IllegalStateException> {
            svc.startNewJoinedGame(msg)
        }
    }

    /**
     * startNewJoinedGame exercises all token-count branches (3,4,else and non-first)
     */
    @Test
    fun startNewJoinedGame_tokenCountBranches() {
        val svc = NetworkService(RootService())
        svc.updateConnectionState(ConnectionState.WAITING_FOR_INIT)

        run {
            val players3 = listOf(
                edu.udo.cs.sopra.ntf.subtypes.Player("A", Color.ORANGE),
                edu.udo.cs.sopra.ntf.subtypes.Player("B", Color.BLUE),
                edu.udo.cs.sopra.ntf.subtypes.Player("C", Color.WHITE)
            )
            val msg3 = InitMessage(drawPile = emptyList(), isFirstGame = true, players = players3)
            assertFails { svc.startNewJoinedGame(msg3) }
        }

        run {
            val players4 = listOf(
                edu.udo.cs.sopra.ntf.subtypes.Player("A", Color.ORANGE),
                edu.udo.cs.sopra.ntf.subtypes.Player("B", Color.BLUE),
                edu.udo.cs.sopra.ntf.subtypes.Player("C", Color.WHITE),
                edu.udo.cs.sopra.ntf.subtypes.Player("D", Color.BLACK)
            )
            val msg4 = InitMessage(drawPile = emptyList(), isFirstGame = true, players = players4)
            assertFails { svc.startNewJoinedGame(msg4) }
        }

        run {
            val players = listOf(
                edu.udo.cs.sopra.ntf.subtypes.Player("A", Color.ORANGE),
                edu.udo.cs.sopra.ntf.subtypes.Player("B", Color.BLUE),
                edu.udo.cs.sopra.ntf.subtypes.Player("C", Color.WHITE),
                edu.udo.cs.sopra.ntf.subtypes.Player("D", Color.BLACK),
            )
            val msgNotFirst = InitMessage(drawPile = emptyList(), isFirstGame = false, players = players)
            assertFails { svc.startNewJoinedGame(msgNotFirst) }
        }

}
}