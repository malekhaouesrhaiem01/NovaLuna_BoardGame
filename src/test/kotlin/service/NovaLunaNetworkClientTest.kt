/*
ackage service
import edu.udo.cs.sopra.ntf.messages.InitMessage
import edu.udo.cs.sopra.ntf.messages.TurnMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import org.junit.jupiter.api.Assertions.assertNull
import edu.udo.cs.sopra.ntf.subtypes.Player as NetworkPlayer
/**
 * Unit test class for [NovaLunaNetworkClient].
 *
 * This class verifies that the client reacts appropriately to various network responses and messages,
 * such as successful or failed game creation, join responses, in-game actions, and player interactions.
 *
 * The tests also ensure the client enforces expected connection states, handles edge cases,
 * and correctly delegates to the [NetworkService] when appropriate.
 */
class NovaLunaNetworkClientTest {

    private lateinit var networkService: NetworkService
    private lateinit var client: NovaLunaNetworkClient

    @BeforeEach
    fun setup() {
       // networkService = mockk(relaxed = true)
       // every { networkService.connectionState } returnsMany listOf(
            ConnectionState.WAITING_FOR_HOST_CONFIRMATION,
            ConnectionState.WAITING_FOR_JOIN_CONFIRMATION,
            ConnectionState.WAITING_FOR_GUESTS,
            ConnectionState.PLAYING_MY_TURN,
            ConnectionState.WAITING_FOR_OPPONENT
        )

        client = NovaLunaNetworkClient(
            playerName = "Alice",
            host = "localhost",
            secret = "secret",
            networkService = networkService
        )
    }
    /**
    * Verifies that a successful create game response sets the session ID and informs the service.
    */
    @Test
    fun `onCreateGameResponse with success sets sessionID and calls service`() {
        val response = CreateGameResponse(sessionID = "123", status = CreateGameResponseStatus.SUCCESS)
        client.onCreateGameResponse(response)

        assertEquals("123", client.sessionID)
        verify { networkService.onCreateGameResponse(response, "Alice") }
    }
    /**
     * Verifies that a failed create game response causes disconnection and error handling.
     */
    @Test
    fun `onCreateGameResponse with failure disconnects and errors`() {
        val response = CreateGameResponse(sessionID = null,
            status = CreateGameResponseStatus.ALREADY_ASSOCIATED_WITH_GAME)
        assertThrows(IllegalStateException::class.java) {
            client.onCreateGameResponse(response)
        }
        verify { networkService.disconnect() }
    }
    /**
     * Verifies that a successful join game response sets the session ID and opponent name.
     */
    @Test
    fun `onJoinGameResponse with success sets sessionID and otherPlayerName`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_JOIN_CONFIRMATION

        val response = JoinGameResponse(
            sessionID = "456",
            status = JoinGameResponseStatus.SUCCESS,
            opponents = listOf("Bob"),
            message = "OK"
        )
        client.onJoinGameResponse(response)

        assertEquals("456", client.sessionID)
        assertEquals("Bob", client.otherPlayerName)
        verify { networkService.onJoinGameResponse(response, "Alice") }
    }
    /**
     * Verifies that a failed join game response leads to disconnection and error handling.
     */
    @Test
    fun `onJoinGameResponse with failure disconnects and errors`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_JOIN_CONFIRMATION

        val response = JoinGameResponse(
            sessionID = null,
            status = JoinGameResponseStatus.PLAYER_NAME_ALREADY_TAKEN,
            opponents = emptyList(),
            message = "Name conflict"
        )
        assertThrows(IllegalStateException::class.java) {
            client.onJoinGameResponse(response)
        }
        verify { networkService.disconnect() }
    }

    /**
     * Verifies that when a player joins, their name is stored and the service is notified.
     */
    @Test
    fun `onPlayerJoined stores name and notifies service`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_GUESTS

        val notification = PlayerJoinedNotification(sender = "Charlie", message = "Player joined")
        client.onPlayerJoined(notification)

        assertEquals("Charlie", client.otherPlayerName)
        verify { networkService.onPlayerJoined(notification) }
    }
    /**
     * Verifies that a successful game action response does not trigger disconnect.
     */
    @Test
    fun `onGameActionResponse with success does nothing`() {
        every { networkService.connectionState } returns ConnectionState.PLAYING_MY_TURN

        val response = GameActionResponse(
            status = GameActionResponseStatus.SUCCESS,
            errorMessages = emptyMap()
        )
        client.onGameActionResponse(response)

        verify(exactly = 0) { networkService.disconnect() }
    }
    /**
     * Verifies that a failed game action response causes a disconnect and error.
     * */
    @Test
    fun `onGameActionResponse with failure disconnects and errors`() {
        every { networkService.connectionState } returns ConnectionState.PLAYING_MY_TURN

        val response = GameActionResponse(
            status = GameActionResponseStatus.INVALID_JSON,
            errorMessages = emptyMap()
        )
        assertThrows(IllegalStateException::class.java) {
            client.onGameActionResponse(response)
        }
        verify { networkService.disconnect() }
    }

    /**
     * Verifies that InitMessage is passed to the service as expected.
     */
    @Test
    fun `onInitReceived delegates to startNewJoinedGame`() {
        val message = InitMessage(drawPile = emptyList(), isFirstGame = true, players = emptyList())
        client.onInitReceived(message, "Host")
        verify { networkService.startNewJoinedGame(message) }
    }
    /**
     * Verifies that TurnMessage is passed to the service correctly.
     */
    @Test
    fun `onTurnReceived delegates to receiveTurnMessage`() {
        val message = TurnMessage(tileId = 1, x = 0, y = 0, refillTrack = false)
        client.onTurnReceived(message, "Host")
        verify { networkService.receiveTurnMessage(message) }
    }
    /**
     * Ensures that an empty opponent list results in null as otherPlayerName.
     */
    @Test
    fun `onJoinGameResponse with empty opponents list sets name to null`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_JOIN_CONFIRMATION

        val response = JoinGameResponse(
            sessionID = "789",
            status = JoinGameResponseStatus.SUCCESS,
            opponents = emptyList(),
            message = "Corrupted"
        )

        client.onJoinGameResponse(response)

        assertEquals("789", client.sessionID)
        assertNull(client.otherPlayerName) //
    }
    /**
     * Verifies that create game response in an invalid state throws an exception.
     */
    @Test
    fun `onCreateGameResponse throws if in unexpected connection state`() {
        every { networkService.connectionState } returns ConnectionState.PLAYING_MY_TURN

        val response = CreateGameResponse(
            sessionID = "000",
            status = CreateGameResponseStatus.SUCCESS
        )
        assertThrows(IllegalStateException::class.java) {
            client.onCreateGameResponse(response)
        }
    }
    /**
     * Ensures that unknown senders do not break InitMessage handling.
     */
    @Test
    fun `onInitReceived with unknown sender still works`() {
        val message = InitMessage(
            drawPile = emptyList(),
            isFirstGame = true,
            players = listOf()
        )

        client.onInitReceived(message, sender = "🤖 UnknownBot")
        verify { networkService.startNewJoinedGame(message) }
    }
    /**
     * Verifies that negative tile values in TurnMessage are still accepted and delegated.
     */
    @Test
    fun `onTurnReceived with negative tileId delegates anyway`() {
        val message = TurnMessage(tileId = -99, x = -1, y = -1, refillTrack = true)
        client.onTurnReceived(message, sender = "Host")
        verify { networkService.receiveTurnMessage(message) }
    }
    /**
     * Tests that InitMessage with a large draw pile and mocked players works as expected.
     */
    @Test
    fun `onInitReceived handles large draw pile with mocked players`() {
        val drawPile = List(10_000) { it } // simulate large draw pile

        val players = listOf(
            mockk<NetworkPlayer>(relaxed = true),
            mockk<NetworkPlayer>(relaxed = true)
        )

        val message = InitMessage(
            drawPile = drawPile,
            isFirstGame = false,
            players = players
        )

        client.onInitReceived(message, sender = "Host")

        verify { networkService.startNewJoinedGame(message) }
    }

    /**
     * Verifies that TurnMessage handles edge-case integer values.
     */
    @Test
    fun `onTurnReceived with edge coordinates`() {
        val message = TurnMessage(
            tileId = Int.MAX_VALUE,
            x = Int.MIN_VALUE,
            y = Int.MAX_VALUE,
            refillTrack = false
        )

        client.onTurnReceived(message, sender = "Host")
        verify { networkService.receiveTurnMessage(message) }
    }
    /**
     * Ensures that a null sessionID in a successful create response throws an exception.
     */
    @Test

    fun `onCreateGameResponse with success but null sessionID throws`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_HOST_CONFIRMATION

        val response = CreateGameResponse(
            sessionID = null,
            status = CreateGameResponseStatus.SUCCESS
        )

        assertThrows(IllegalArgumentException::class.java) {
            client.onCreateGameResponse(response)
        }
    }
    /**
     * Verifies that if multiple opponents are returned, the first one is chosen.
     */
    @Test
    fun `onJoinGameResponse with multiple opponents picks first as otherPlayerName`() {
        every { networkService.connectionState } returns ConnectionState.WAITING_FOR_JOIN_CONFIRMATION

        val response = JoinGameResponse(
            sessionID = "multi-opponents",
            status = JoinGameResponseStatus.SUCCESS,
            opponents = listOf("Bob", "Charlie", "Dana"),
            message = "OK"
        )

        client.onJoinGameResponse(response)

        assertEquals("multi-opponents", client.sessionID)
        assertEquals("Bob", client.otherPlayerName) //  pick the first
    }

}
*/