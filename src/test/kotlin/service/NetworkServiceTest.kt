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

class NetworkServiceTest {

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

    @Test
    fun testStartNewHostedGame() {
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        val players = listOf(
            Player("Paula",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                0),
            Player("Player2",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.ORANGE,
                mutableListOf(),
                0)
        )

        assertThrows<IllegalStateException> {networkService.startNewHostedGame(players,
            isFirstGame = false, randomOrder = true)}

        networkService.hostGame("neumond25", "Paula", "Session1")

        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)

        assertThrows<IllegalArgumentException> { networkService.startNewHostedGame(
            listOf(players[0]),
            isFirstGame = false, randomOrder = true) }

        networkService.startNewHostedGame(players, isFirstGame = false, randomOrder = true)


        val players2 = listOf(
            Player("Player1",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                0),
            Player("Paula",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.ORANGE,
                mutableListOf(),
                0)
        )
        rootService.currentGame = null
        networkService.disconnect()
        networkService.hostGame("neumond25", "Paula", "Session2")
        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        networkService.startNewHostedGame(players2, isFirstGame = false, randomOrder = true)
    }

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

    @Test
    fun testReceiveTurnMessage(){
        val rootService = RootService()
        val networkService = NetworkService(rootService)

        assertThrows<IllegalStateException> { networkService.receiveTurnMessage(
            TurnMessage(30, 0, 0, refillTrack = false)) }

        val players1 = listOf(
            Player("Player1",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                0),
            Player("Paula",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.ORANGE,
                mutableListOf(),
                0)
        )
        networkService.hostGame("neumond25", "Paula", "Session2")
        networkService.onPlayerJoined(PlayerJoinedNotification("Hallo", "Nochmal Hallo"))
        networkService.startNewHostedGame(players1, isFirstGame = false, randomOrder = false)

        val tileTrack = rootService.currentGame?.tileTrack!!
        val message = TurnMessage(tileTrack[1]!!.id, 0, 0, refillTrack = false)
        networkService.receiveTurnMessage(message)
    }

}
