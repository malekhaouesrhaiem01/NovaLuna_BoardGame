package service
import entity.Player
import entity.PlayerColour
import entity.PlayerType
import org.junit.jupiter.api.assertThrows
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
            Player("Player1",
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

    }
}
