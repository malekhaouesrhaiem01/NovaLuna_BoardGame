package service

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import kotlin.test.*
/**
 * Test class for the method `moveMeepleAndPlayer()` in [GameService].
 *
 * This class verifies that both the meeple (on the tile track) and the current player's moon track position
 * are updated correctly after a tile is selected and taken.
 */
class MoveMeepleAndPlayerTest {
    private lateinit var  rootService: RootService

    /**
     * setup for a two player Game before each test
     */
    @BeforeTest
    fun setUp()
    {
        rootService = RootService()
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
    }
    /**
     * Tests that `moveMeepleAndPlayer()`:
     * - removes the selected tile from the tile track,
     * - clears the tile's moon track position,
     * - and increases the active player's moon track position by the tile's time cost.
     */
    @Test
    fun testMoveMeepleAndPlayer(){
        val game = rootService.currentGame!!

        val tile = game.tileTrack[2]
        checkNotNull(tile)
        tile.moonTrackPosition = 2
        val time = tile.time
        val playerMoonTrackPos = game.players[game.activePlayer].moonTrackPosition


        rootService.gameService.moveMeepleAndPlayer(tile)

        assert(game.tileTrack[2] == null)
        assert(tile.moonTrackPosition == null)
        assert(game.players[game.activePlayer].moonTrackPosition == playerMoonTrackPos + time)

    }
}