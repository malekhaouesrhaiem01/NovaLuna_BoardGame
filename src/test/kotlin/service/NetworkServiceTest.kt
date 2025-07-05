package service

import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

class NetworkServiceTest {

    private lateinit var rootService: RootService
    private lateinit var networkService: NetworkService

    @BeforeEach
    fun setUp() {
        // Use a real RootService instead of mocking it
        rootService = RootService()
        networkService = NetworkService(rootService)
    }

    @Test
    fun onCreateGameResponse_populatesLobbyAndUpdatesState() {
        // 1) build a real CreateGameResponse instance:
        val response = CreateGameResponse(
            sessionID = "SESSION123",
            status    = CreateGameResponseStatus.SUCCESS
            // (if there’s a third constructor param for opponents you can pass emptyList())
        )

        // 2) call the handler
        networkService.onCreateGameResponse(response, hostName = "Alice")

        // 3) assertions
        assertEquals(listOf("Alice"), networkService.currentSessionPlayers)
        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)
    }

    @Test
    fun onJoinGameResponse_preloadsHostAndSelfAndUpdatesState() {
        val response = JoinGameResponse(
            sessionID = "SESSION123",
            status    = JoinGameResponseStatus.SUCCESS,
            opponents = listOf("Alice"),
            message = "hello"
        )
        networkService.onJoinGameResponse(response, guestName = "Bob")

        assertEquals(listOf("Alice", "Bob"), networkService.currentSessionPlayers)
        assertEquals(ConnectionState.WAITING_FOR_INIT, networkService.connectionState)
    }


    @Test
    fun onPlayerJoined_appendsNewGuestAndRemainsInWaitingForGuests() {
        // Prepare lobby with host using a real CreateGameResponse
        val createResp = CreateGameResponse(
            sessionID = "S1",
            status    = CreateGameResponseStatus.SUCCESS
        )
        networkService.onCreateGameResponse(createResp, hostName = "Alice")
        assertEquals(ConnectionState.WAITING_FOR_GUESTS, networkService.connectionState)

        // Act: a second player joins
        val notification = PlayerJoinedNotification(
         "S1",
            sender    = "Bob"
        )
        networkService.onPlayerJoined(notification)

        // Assert
        assertEquals(listOf("Alice", "Bob"), networkService.currentSessionPlayers)
        assertEquals(
            ConnectionState.WAITING_FOR_GUESTS,
            networkService.connectionState,
            "After onPlayerJoined, should remain WAITING_FOR_GUESTS"
        )
    }

    @Test
    fun sendTurnMessage_throwsIfNotInPlayingMyTurn() {
        // Initial state is DISCONNECTED
        val ex = assertThrows<IllegalStateException> {
            networkService.sendTurnMessage(tileId = 5, x = 1, y = 2, refillTrack = false)
        }
        assertTrue(ex.message!!.contains("Cannot send turn"))
    }

    @Test
    fun receiveTurnMessage_throwsIfNotInWaitingForOpponent() {
        // Create a real TurnMessage (tileId, x, y, refillTrack)
        val turnMsg = TurnMessage(
            tileId      = 42,
            x           = 1,
            y           = 2,
            refillTrack = false
        )

        // connectionState is still DISCONNECTED, so we should get IllegalStateException
        val ex = assertThrows<IllegalStateException> {
            networkService.receiveTurnMessage(turnMsg)
        }
        assertTrue(ex.message!!.contains("Not expecting an opponent move"))
    }


    //--------------------------------------------------------------------



    }