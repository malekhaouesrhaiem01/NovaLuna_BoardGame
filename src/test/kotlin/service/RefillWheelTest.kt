package service

import entity.*
import kotlin.test.*

/**
 * Test class for method refillWheel() from class PlayerService.kt
 */
class RefillWheelTest
{
    private lateinit var  rootService: RootService

    /**
     * setup for a two player Game before each test
     */
    @BeforeTest
    fun setUp()
    {
        rootService = RootService()
        val players = listOf(
            Player("Player1",
                18,
                0,
                false,
                PlayerType.HUMAN,
                PlayerColour.WHITE,
                mutableListOf(),
                1),
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
     * Test for an empty Wheel getting refilled and must have 11 Tiles in the end.
     */
    @Test
    fun testRefillsWhenWheelEmpty()
    {
        val game = rootService.currentGame!!
        for (i in 1 until game.tileTrack.size) game.tileTrack[i] = null
        val drawBefore = game.drawPile.size

        rootService.playerActionService.refillWheel()

        assertEquals(11, game.tileTrack.count { it != null })
        assertEquals(drawBefore - 11, game.drawPile.size)
    }

    /**
     * Test for exactly 2 tiles remain and if refill chosen it must have 11 tiles in the end
     */
    @Test
    fun testRefillsTwoTilesRemain()
    {
        val game = rootService.currentGame!!
        for (i in 1 until game.tileTrack.size) if (i !in listOf(2, 7)) game.tileTrack[i] = null
        val drawBefore = game.drawPile.size

        rootService.playerActionService.refillWheel()

        assertEquals(11, game.tileTrack.count { it != null })
        assertEquals(drawBefore - 9, game.drawPile.size)
    }

    /**
     * Test for three or more tiles present -> method must do nothing
     */
    @Test
    fun testNothingAtThreeOrMoreTiles()
    {
        val game = rootService.currentGame!!
        val wheelBefore = game.tileTrack.map { it }
        val drawBefore = game.drawPile.size

        rootService.playerActionService.refillWheel()

        assertEquals(wheelBefore, game.tileTrack)
        assertEquals(drawBefore, game.drawPile.size)
    }
}