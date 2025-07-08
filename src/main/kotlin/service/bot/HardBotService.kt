package service.bot

import entity.Move
import service.RootService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Service class for the Hard Bot player
 *
 * @property rootService The root service providing access to other services.
 */
class HardBotService(private val rootService: RootService) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    /**
     * Executes a move for the hard bot player.
     *
     * This method checks if the current player is a hard bot, calculates the best move using Monte Carlo Tree Search
     * (MCTS), and then plays the move after a delay determined by the simulation speed.
     *
     * @throws IllegalStateException if the current player is not a hard bot or if no possible moves are available.
     */
    fun executeHardBotMove() {
        val game = rootService.currentGame
        checkNotNull(game) { "Game is null" }

        val currentPlayer = game.players[game.activePlayer]
        check(currentPlayer.playerType == entity.PlayerType.HARDBOT) { "Current player is not a HARDBOT" }

        val possibleMoves = rootService.gameService.getPossibleMovesForCurrentPlayer()
        check(possibleMoves.isNotEmpty()) { "No possible moves for the hard bot" }

        val delay = when (game.simulationSpeed) {
            0 -> 0L
            1 -> 1000L
            3 -> 3000L
            else -> 1000L // Default delay
        }

        Thread {
            val bestMove = findBestMove(game, possibleMoves, System.currentTimeMillis() + 9500)
            scheduler.schedule({
                rootService.playerActionService.playTile(bestMove)
                scheduler.schedule({
                    rootService.gameService.endTurn()
                }, 1, TimeUnit.SECONDS)
            }, delay, TimeUnit.MILLISECONDS)
        }.start()
    }

    /**
     * Finds the best move for the hard bot using MCTS.
     *
     * This method creates a root node for the MCTS, runs a number of simulations to find the best move, and then
     * returns the best move.
     *
     * @param game The current game state.
     * @param moves The list of possible moves.
     * @return The best move found by the MCTS.
     */
    private fun findBestMove(game: entity.NovaLunaGame, moves: List<Move>, endTime: Long): Move {
        val root = MCTSNode(game.clone(), untriedMoves = moves.toMutableList())

        while (System.currentTimeMillis() < endTime) {
            val node = root.selectBestChild() ?: root
            val winner = node.rollout()
            node.backpropagate(winner)
        }

        return root.children.maxByOrNull { it.visits }?.moveThatLedHere ?: moves[Random.nextInt(moves.size)]
    }
}