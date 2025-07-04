package service.bot

import entity.Move
import entity.NovaLunaGame
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Represents a single node in the Monte Carlo Search Tree.
 * Each node stores a specific game state and the statistics gathered from simulations
 * that have passed through it.
 *
 * @param gameState The [entity.NovaLunaGame] instance that this node represents. This should be
 *   a deep copy of the game state to ensure the MCTS does not alter the actual game.
 * @param parent The parent node in the tree. This is `null` for the root node of the search tree.
 * @param moveThatLedHere The [entity.Move] that was applied to the parent's state to reach this state.
 *   This is `null` for the root node.
 * @param untriedMoves A mutable list of [entity.Move]s that are possible from this node's state
 *   but have not yet been used to create a child node (i.e., not yet expanded).
 */
class MCTSNode(
    val gameState: NovaLunaGame,
    val parent: MCTSNode? = null,
    val moveThatLedHere: Move? = null,
    val untriedMoves: MutableList<Move> // List of moves not yet expanded
) {
    /**
     * The number of times this node has been visited during the MCTS search.
     * Used in the UCT calculation.
     */
    var visits: Int = 0

    /**
     * A mutable map storing the cumulative scores for each player based on simulations
     * that passed through this node. The key is the player's ID ([Int]), and the value
     * is their accumulated score ([Double]).
     */
    val scores: MutableMap<Int, Double> = mutableMapOf() // Map from player index to score

    /**
     * A mutable list of child nodes that have been created by expanding this node.
     * Each child represents a possible move from this node's state.
     */
    val children: MutableList<MCTSNode> = mutableListOf()

    /**
     * Checks if all possible moves from this node's state have already been used
     * to create child nodes (i.e., if the node is fully expanded).
     *
     * @return `true` if there are no untried moves left; `false` otherwise.
     */
    fun isFullyExpanded(): Boolean = untriedMoves.isEmpty()

    /**
     * Checks if the game state represented by this node is a terminal state (i.e., the game has ended).
     * This determination should be based on the rules of Nova Luna.
     *
     * @return `true` if the game is over in this state; `false` otherwise.
     */
    fun isTerminal(): Boolean {
        // A player has placed all their tokens. We check this for ALL players, not just the active one.
        if (gameState.players.any { it.tokenCount < 1 }) {
            return true
        }

        // The tile track and draw pile are both empty, so no more moves can be made.
        if (gameState.tileTrack.isEmpty() && gameState.drawPile.isEmpty()) {
            return true
        }
        return false
    }

    /**
     * Selects the best child node from this node's children based on the UCT (Upper Confidence Bound for Trees)
     * formula.
     * The UCT formula balances between exploitation (choosing children with high average rewards)
     * and exploration (choosing children that have not been visited often).
     *
     * The formula for UCT is: `Q(v) + C * sqrt(ln(N(p)) / N(v))`
     * where:
     * - `Q(v)` is the average reward of child node `v`.
     * - `N(v)` is the number of times child node `v` has been visited.
     * - `N(p)` is the number of times the parent node `p` has been visited.
     * - `C` is the exploration constant (`explorationConstant`).
     *
     * Preconditions:
     * - This node must have at least one child in its `children` list.
     *
     * Postconditions:
     * - The state of this node and its children remains unchanged.
     *
     * @param explorationConstant The exploration parameter 'C' in the UCT formula. A common value is `sqrt(2.0)`.
     * @return The [MCTSNode] representing the child with the highest UCT value, or `null` if this node has no children.
     */
    fun selectBestChild(explorationConstant: Double = 1.41): MCTSNode? {
        if (children.isEmpty()) return null

        val parentVisits = visits.toDouble()
        if (parentVisits == 0.0) return children.random()

        val playerId = gameState.activePlayer  // Spieler, für den wir die Bewertung machen

        return children.maxByOrNull { child ->
            val childVisits = child.visits.toDouble()
            val playerScore = child.scores[playerId] ?: 0.0

            if (childVisits == 0.0) {
                Double.MAX_VALUE
            } else {
                val exploitation = playerScore / childVisits
                val exploration = explorationConstant * sqrt(ln(parentVisits) / childVisits)
                exploitation + exploration
            }
        }
    }
}