package service.bot

import entity.PlayerType
import service.AbstractRefreshingService
import service.RootService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

/**
 * Service to handle the easy bot's logic.
 * The bot selects a move randomly from all available legal moves.
 *
 * @property rootService A reference to the [service.RootService] to access other services like
 * [service.GameService] and [service.PlayerActionService].
 */
class EasyBotService(private val rootService: RootService) : AbstractRefreshingService() {


    /**
     * Executes a complete turn for the easy bot.
     * The bot selects a random move from the set of all allowed moves and executes it.
     *
     * Preconditions:
     * - A game must be in progress (`rootService.currentGame != null`).
     * - The currently active player must be an [entity.PlayerType.EASYBOT].
     * - There must be at least one possible move for the bot.
     *
     * Postconditions:
     * - A valid move has been executed via the [service.PlayerActionService].
     * - The game state in [entity.NovaLunaGame] has been updated (a tile was placed, the meeple was moved, tasks may have been completed).
     * - It is now the next player's turn.
     *
     * @throws IllegalStateException if any of the preconditions are violated.
     */
    fun executeEasyMove() {
        // Get required services and the current game state
        val game = rootService.currentGame
            ?: throw IllegalStateException("executeEasyMove called but no game is in progress.")

        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Precondition: Check if the current player is an EASYBOT
        val currentPlayer = game.players[game.activePlayer]
        if (currentPlayer.playerType != PlayerType.EASYBOT) {
            throw IllegalStateException("executeEasyMove called, but the current player is not an EASYBOT.")
        }
        val waitTime = game.simulationSpeed.toLong()
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        // 1. Get all possible moves from the single source of truth
        val possibleMoves = gameService.getPossibleMovesForCurrentPlayer()

        // Precondition: Check if there is at least one move
        if (possibleMoves.isEmpty()) {
            // According to the rules, a player might have to pass their turn if no move is possible.
            // For now, we treat it as an unexpected state as per KDoc.
            throw IllegalStateException("EASYBOT's turn, but no moves are available.")
        }
        // 2. Select a random move
        val randomMove = possibleMoves.random()
        println("EasyBot selected move: Place tile ${randomMove.tile?.id} at ${randomMove.position}")
        // 3. Execute the move
        playerActionService.playTile(randomMove)
        scheduler.schedule({
            SwingUtilities.invokeLater {
                gameService.endTurn()
            }
        }, waitTime, TimeUnit.SECONDS)
    }
}