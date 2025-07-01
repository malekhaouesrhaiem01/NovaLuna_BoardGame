package service.bot

import entity.Move
import entity.NovaLunaGame
import entity.PlayerType
import service.AbstractRefreshingService
import service.RootService

/**
 * Service for the hard bot, which uses a Monte Carlo Tree Search (MCTS) algorithm
 * to determine the best move.
 * It executes the four phases (Selection, Expansion, Simulation, Backpropagation) in a loop
 * to find the best move for a given game state within a time limit.
 *
 * @property rootService A reference to the [service.RootService] to access other services.
 */
class HardBotService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Main entry point for the hard bot's turn.
     * This method initiates and controls the Monte-Carlo Tree Search (MCTS) algorithm
     * to find the best possible move within a time limit, and then executes it.
     *
     * Preconditions:
     * - A game must be in progress (`rootService.currentGame != null`).
     * - The currently active player must be a [entity.PlayerType.HARDBOT].
     *
     * Postconditions:
     * - The best found move has been executed via the [service.PlayerActionService].
     * - The game state in [entity.NovaLunaGame] has been updated.
     * - It is now the next player's turn.
     * - The total execution time of this method, including the MCTS search, did not exceed 10 seconds.
     *
     * @returns This method has no return value (`Unit`).
     * @throws IllegalStateException if any of the preconditions are violated.
     */
    fun findAndExecuteBestMove() {
        val game = rootService.currentGame
            ?: throw IllegalStateException("findAndExecuteBestMove called but no game is in progress.")

        // Precondition: Check if the current player is a HARDBOT
        val currentPlayer = game.players[game.activePlayer]
        if (currentPlayer.playerType != PlayerType.HARDBOT) {
            throw IllegalStateException("findAndExecuteBestMove called, but the current player is not a HARDBOT.")
        }

        // TODO: Ensure a deep copy of the game state for MCTS to avoid modifying the actual game state.
        // val rootState = game.deepCopy()
        val rootState = game // Placeholder: This needs to be a deep copy!

        // Run MCTS for a maximum of 10 seconds (9.5 seconds to allow for some buffer).
        val timeLimitMillis = 9500L

        val bestMove = findBestMoveInternal(rootState, timeLimitMillis)

        // TODO: Execute the found move using the PlayerActionService.
        println("HardBot selected move: Place tile ${bestMove.tile?.id} at ${bestMove.position}")
    }

    /**
     * Runs the Monte Carlo Tree Search (MCTS) algorithm to find the best move
     * from a given game state within a specified time limit.
     * This method orchestrates the four MCTS phases: Selection, Expansion, Simulation, and Backpropagation.
     *
     * @param rootState The starting game state for the search (should be a deep copy of the actual game state).
     * @param timeLimitMillis The maximum time in milliseconds for the search to run.
     * @return The best [entity.Move] identified by the MCTS algorithm.
     * @throws IllegalStateException if no suitable move could be found (e.g., if the game is already over or no moves are possible).
     */
    private fun findBestMoveInternal(rootState: NovaLunaGame, timeLimitMillis: Long): Move {
        // Not finished yet

        val gameService = rootService.gameService

        val initialPossibleMoves = gameService.getPossibleMovesForCurrentPlayer() // This assumes it uses rootService.currentGame

        val rootNode = MCTSNode(
            gameState = rootState,
            untriedMoves = initialPossibleMoves.toMutableList()
        )

        val endTime = System.currentTimeMillis() + timeLimitMillis
        while (System.currentTimeMillis() < endTime) {
            // The 4 phases of MCTS
            val promisingNode = selection(rootNode)
            if (!promisingNode.isTerminal()) {
                val expandedNode = expansion(promisingNode)
                val simulationResult = simulate(expandedNode.gameState) // Simulate from the expanded node's state
                backpropagate(expandedNode, simulationResult)
            } else {
                // If the promising node is terminal, just backpropagate its result.
                // This means a winning/losing state was reached directly.
                backpropagate(promisingNode, simulate(promisingNode.gameState)) // Simulate a terminal state to get results
            }
        }

        // After the time is up, choose the best child of the root node based on visits/scores.
        val bestChild = rootNode.children.maxByOrNull { it.visits } // Or based on score, depending on strategy
            ?: throw IllegalStateException("MCTS could not find a move within the time limit or no moves were possible.")

        return bestChild.moveThatLedHere
            ?: throw IllegalStateException("Best MCTS child node has no associated move.")
    }

    /**
     * Internal MCTS Method (Phase 1: Selection).
     * Traverses the search tree downwards from the given [node] (usually the root).
     * At each step, the most promising child is selected using the UCT formula ([MCTSNode.selectBestChild])
     * until a node is reached that is either not fully expanded or is a terminal state.
     *
     * Preconditions:
     * - [node] is a valid node within the current search tree.
     *
     * Postconditions:
     * - The game state remains unchanged. This method is a query.
     *
     * @param node The starting node for the selection phase.
     * @return The selected [MCTSNode] for further expansion or simulation.
     */
    private fun selection(node: MCTSNode): MCTSNode {
        TODO("Selection phase not yet implemented")
    }

    /**
     * Internal MCTS Method (Phase 2: Expansion).
     * Expands the search tree at the given [node] by selecting one untried move and
     * creating a new child node for it.
     *
     * Preconditions:
     * - The given [node] is not terminal and not fully expanded (`isFullyExpanded()` is `false`).
     *
     * Postconditions:
     * - A new child node has been added to the [node]'s `children` list.
     * - The [node]'s `untriedMoves` list has one less element.
     * - The game state remains unchanged (the new child node holds a *copy* of the state after the move).
     *
     * @param node The node to be expanded.
     * @return The newly created child node.
     * @throws IllegalStateException if the method is called on an already fully expanded node.
     */
    private fun expansion(node: MCTSNode): MCTSNode {
        TODO("Expansion phase not yet implemented.")
    }

    /**
     * Internal MCTS Method (Phase 3: Simulation / Rollout).
     * Performs a fast, lightweight simulation (rollout) from the given [state] until the end of the game.
     * Instead of recursively calling MCTS, moves are selected based on a simple, fast heuristic (e.g., random moves).
     *
     * Preconditions:
     * - [state] is a deep copy of a game state from an [MCTSNode].
     *
     * Postconditions:
     * - The original game state of the application remains untouched.
     *
     * @param state The starting state for the simulation.
     * @return A [Map] where keys are player IDs ([Int]) and values are their final scores/outcomes ([Double])
     *         (e.g., `{playerId: 1.0}` for winner, `{playerId: 0.0}` for loser).
     */
    private fun simulate(state: NovaLunaGame): Map<Int, Double> {
        TODO("Simulation phase not yet implemented.")
    }

    /**
     * Internal MCTS Method (Phase 4: Backpropagation).
     * Updates the statistics (`visits` and `scores`) of the nodes.
     * Starting from the [node] where the simulation began, the path is traced backward to the root of the search tree,
     * and each node along the way is updated with the [result] of the simulation.
     *
     * Preconditions:
     * - A simulation has been completed and yielded a [result].
     * - [node] is the node from which the simulation was started.
     *
     * Postconditions:
     * - The `visits` and `scores` attributes of all nodes on the path to the root are updated.
     *
     * @param node The starting node for the backpropagation.
     * @param result The result of the simulation to be propagated.
     * @returns This method has no return value (`Unit`).
     */
    private fun backpropagate(node: MCTSNode, result: Map<Int, Double>) {
        TODO("Backpropagation phase not yet implemented.")
    }
}