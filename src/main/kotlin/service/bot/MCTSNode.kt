package service.bot

import entity.Move
import entity.NovaLunaGame
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Represents a node in the Monte Carlo Tree Search (MCTS) tree.
 *
 * @property gameState The game state associated with this node.
 * @property parent The parent node of this node.
 * @property moveThatLedHere The move that led to this node.
 * @property untriedMoves The list of moves that have not yet been tried from this node.
 * @property children The list of child nodes of this node.
 * @property visits The number of times this node has been visited.
 * @property scores The scores of this node for each player.
 */
class MCTSNode(
    val gameState: NovaLunaGame,
    val parent: MCTSNode? = null,
    val moveThatLedHere: Move? = null,
    var untriedMoves: MutableList<Move>,
    val children: MutableList<MCTSNode> = mutableListOf(),
    var visits: Int = 0,
    val scores: MutableMap<Int, Double> = mutableMapOf()
) {

    /**
     * Selects the best child of this node to explore.
     *
     * This method uses the UCT (Upper Confidence Bound 1 applied to trees) formula to select the best child.
     *
     * @param explorationConstant The exploration constant to use in the UCT formula.
     * @return The best child node to explore.
     */
    fun selectBestChild(explorationConstant: Double = 1.41): MCTSNode? {
        if (children.isEmpty()) return null
        return children.maxByOrNull { uct(it, explorationConstant) }
    }

    /**
     * Expands this node by creating a new child node from an untried move.
     *
     * @return The new child node.
     */
    fun expand(): MCTSNode {
        val move = untriedMoves.removeAt(0)
        val newGameState = gameState.deepCopy()
        // Apply the move to the new game state
        // This is a simplified representation. You'll need to implement the actual logic for applying a move.
        val newPossibleMoves = mutableListOf<Move>() // This should be the possible moves from the new state
        val newNode = MCTSNode(newGameState, this, move, newPossibleMoves)
        children.add(newNode)
        return newNode
    }

    /**
     * Simulates a game from this node's state until a terminal state is reached.
     *
     * @return The winner of the simulated game.
     */
    fun rollout(): Int {
        val currentGameState = gameState.deepCopy()
        while (!isTerminal(currentGameState)) {
            val possibleMoves = getPossibleMoves(currentGameState)
            if (possibleMoves.isEmpty()) {
                return currentGameState.activePlayer
            }
            val move = possibleMoves.random()
            // Apply the move to the current game state
            // This is a simplified representation. You'll need to implement the actual logic for applying a move.
        }
        return getWinner(currentGameState)
    }

    /**
     * Backpropagates the result of a simulation up the tree.
     *
     * @param winner The winner of the simulation.
     */
    fun backpropagate(winner: Int) {
        var currentNode: MCTSNode? = this
        while (currentNode != null) {
            currentNode.visits++
            currentNode.scores.merge(winner, 1.0, Double::plus)
            currentNode = currentNode.parent
        }
    }

    /**
     * Checks if this node is fully expanded.
     *
     * @return `true` if this node is fully expanded, `false` otherwise.
     */
    fun isFullyExpanded(): Boolean = untriedMoves.isEmpty()

    /**
     * Checks if the given game state is a terminal state.
     *
     * @param gameState The game state to check.
     * @return `true` if the game state is terminal, `false` otherwise.
     */
    fun isTerminal(gameState: NovaLunaGame): Boolean {
        // Implement the logic to check for a terminal state (e.g., a player has won or the game is a draw)
        return gameState.players.any { it.tokenCount <= 0 } ||
                (gameState.tileTrack.all { it == null } && gameState.drawPile.isEmpty())
    }

    /**
     * Gets the possible moves from the given game state.
     *
     * @param gameState The game state to get the moves from.
     * @return The list of possible moves.
     */
    private fun getPossibleMoves(gameState: NovaLunaGame): List<Move> {
        try {
            checkNotNull(gameState) { "No game state found" }
        } catch (_: IllegalStateException){}
        // Implement the logic to get the possible moves from the game state
        return emptyList()
    }

    /**
     * Gets the winner from the given game state.
     *
     * @param gameState The game state to get the winner from.
     * @return The winner of the game.
     */
    private fun getWinner(gameState: NovaLunaGame): Int {
        try {
            checkNotNull(gameState) { "No game state found" }
        } catch (_: IllegalStateException){}
        val number = 0
        // Implement the logic to determine the winner from the game state
        return number
    }

    /**
     * Calculates the UCT value for a given node.
     *
     * @param node The node to calculate the UCT value for.
     * @param explorationConstant The exploration constant to use in the UCT formula.
     * @return The UCT value for the given node.
     */
    private fun uct(node: MCTSNode, explorationConstant: Double): Double {
        if (node.visits == 0) return Double.MAX_VALUE
        val exploitation = (node.scores[gameState.activePlayer] ?: 0.0) / node.visits
        val exploration = explorationConstant * sqrt(ln(visits.toDouble()) / node.visits)
        return exploitation + exploration
    }
}