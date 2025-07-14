package service.bot

import entity.Move
import entity.NovaLunaGame
import entity.Player
import entity.Tile
import entity.TileColour
import entity.SerializableCoordinate
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Represents a node in the Monte Carlo Tree Search tree.
 *
 * @property gameState The game state at this node.
 * @property parent The parent node of this node.
 * @property moveThatLedHere The move that led to this node.
 * @property untriedMoves The list of moves that have not yet been tried from this node.
 * @property children The list of child nodes of this node.
 * @property visits The number of times this node has been visited.
 * @property scores A map of player indices to their scores at this node.
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
     * Selects the best child of this node using the UCT formula.
     *
     * @param explorationConstant The exploration constant to use in the UCT formula.
     * @return The best child node.
     */
    fun selectBestChild(explorationConstant: Double = 1.41): MCTSNode? {
        if (children.isEmpty()) return null

        // Dynamic UCT: Lower exploration as we become more confident
        val dynamicExploration = if (visits > 10000) explorationConstant * 0.7 else explorationConstant

        return children.maxByOrNull { child ->
            val uctValue = uct(child, dynamicExploration)
            
            // Add progressive bias for better move ordering
            val bias = calculateMoveBias(child.moveThatLedHere)
            val progressiveBias = bias / (child.visits + 1) * 10 // Decreases as visits increase
            
            uctValue + progressiveBias
        }
    }
    
    private fun calculateMoveBias(move: Move?): Double {
        if (move?.tile == null) return 0.0

        val currentPlayer = gameState.players[gameState.activePlayer]
        val tile = move.tile
        val position = move.position
        var bias = 0.0

        // 1. Task Completion & Chain Reaction Potential
        val taskBias = evaluateTaskCompletionBias(currentPlayer, move)
        bias += taskBias * 30

        // 2. Tile Efficiency (Time vs. Task Potential)
        val efficiency = tile.tasks.size.toDouble() / (tile.time + 1) // +1 to avoid division by zero
        bias += efficiency * 15

        // 3. Strategic Positioning & Tempo
        val adjacentCount = countAdjacentTiles(currentPlayer, position)
        bias += adjacentCount * 10 // Clustering is key

        // Bonus for being ahead on the moon track (tempo)
        val avgMoonTrackPos = gameState.players.map { it.moonTrackPosition }.average()
        if (currentPlayer.moonTrackPosition + tile.time < avgMoonTrackPos) {
            bias += 10
        }

        // 6. Opportunity Heuristic: Reward moves that open up new building spots.
        val openNeighbors = getNeighborPositions(position).count { neighbor ->
            !currentPlayer.tiles.any { it?.position == neighbor }
        }
        bias += (openNeighbors - adjacentCount) * 10 // Strongly reward creating more open spots

        // 4. Endgame Urgency
        if (currentPlayer.tokenCount <= 5) {
            bias += taskBias * 20 // Heavily prioritize finishing
        }

        // 5. Opponent Denial (subtle)
        val opponent = gameState.players[(gameState.activePlayer + 1) % gameState.players.size]
        val opponentTaskBias = evaluateTaskCompletionBias(opponent, move)
        if (opponentTaskBias > 40) { // If the move is a major threat
            bias += opponentTaskBias * 0.1 // Gently nudge the bot to consider blocking
        }

        return bias
    }

    private fun evaluateTaskCompletionBias(player: Player, move: Move): Double {
        val tile = move.tile ?: return 0.0
        val position = move.position
        var completionBias = 0.0
        
        val adjacentColors = mutableMapOf<TileColour, Int>()
        val neighbors = getNeighborPositions(position)
        
        for (neighbor in neighbors) {
            val adjacentTile = player.tiles.find { it?.position == neighbor }
            if (adjacentTile != null) {
                val color = adjacentTile.tileColour
                adjacentColors[color] = adjacentColors.getOrDefault(color, 0) + 1
            }
        }
        
        for ((requirements, isCompleted) in tile.tasks) {
            if (isCompleted) continue
            
            var satisfiedRequirements = 0
            var totalRequirements = 0
            
            for ((requiredColor, requiredCount) in requirements) {
                totalRequirements += requiredCount
                val available = adjacentColors.getOrDefault(requiredColor, 0)
                satisfiedRequirements += minOf(available, requiredCount)
                
                if (available >= requiredCount) {
                    completionBias += 25.0
                }
            }
            
            if (totalRequirements > 0) {
                val progress = satisfiedRequirements.toDouble() / totalRequirements.toDouble()
                completionBias += progress * 10.0
            }
        }
        
        // Check if this helps complete tasks on adjacent tiles
        var adjacentHelped = 0
        for (neighbor in neighbors) {
            val adjacentTile = player.tiles.find { it?.position == neighbor }
            if (adjacentTile != null) {
                for ((requirements, isCompleted) in adjacentTile.tasks) {
                    if (isCompleted) continue
                    
                    // Would adding this tile complete the adjacent task?
                    val adjacentColorsWithNew = adjacentColors.toMutableMap()
                    adjacentColorsWithNew[tile.tileColour] = adjacentColorsWithNew.getOrDefault(tile.tileColour, 0) + 1
                    
                    var wouldComplete = true
                    for ((requiredColor, requiredCount) in requirements) {
                        if (adjacentColorsWithNew.getOrDefault(requiredColor, 0) < requiredCount) {
                            wouldComplete = false
                            break
                        }
                    }
                    
                    if (wouldComplete) {
                        adjacentHelped++
                    }
                }
            }
        }

        // Synergy Bonus: Exponentially reward helping multiple adjacent tasks
        if (adjacentHelped > 0) {
            completionBias += (adjacentHelped * 15.0) * adjacentHelped // 1->15, 2->60, 3->135
        }
        
        return completionBias
    }
    
    
    
    private fun countAdjacentTiles(player: Player, position: SerializableCoordinate): Int {
        val neighbors = getNeighborPositions(position)
        return neighbors.count { neighbor ->
            player.tiles.any { tile -> tile?.position == neighbor }
        }
    }

    /**
     * Expands this node by creating a new child node from an untried move.
     *
     * @return The new child node.
     */
    fun expand(): MCTSNode {
        val move = untriedMoves.removeAt(0)
        val newGameState = gameState.deepCopy()
        
        applyMove(newGameState, move)
        
        val newPossibleMoves = getPossibleMoves(newGameState).toMutableList()
        val newNode = MCTSNode(newGameState, this, move, newPossibleMoves)
        children.add(newNode)
        return newNode
    }

    /**
     * Simulates a game from this node to a terminal state.
     *
     * @return The score of the terminal state.
     */
    fun rollout(): Double {
        val currentGameState = gameState.deepCopy()
        var depth = 0
        val maxRolloutDepth = 30 // Reduced for better performance
        
        while (!isTerminal(currentGameState) && depth < maxRolloutDepth) {
            val possibleMoves = getPossibleMoves(currentGameState)
            if (possibleMoves.isEmpty()) {
                break
            }
            
            // Use a slightly better heuristic for move selection during rollout
            val move = if (possibleMoves.size == 1) {
                possibleMoves[0]
            } else {
                selectBetterRolloutMove(currentGameState, possibleMoves)
            }
            
            applyMove(currentGameState, move)
            depth++
        }
        
        return getTerminalStateScore(currentGameState)
    }

    /**
     * Scores a terminal game state based on the margin of victory.
     * The score is from the perspective of the player whose turn it was at the root of this simulation.
     */
    private fun getTerminalStateScore(gameState: NovaLunaGame): Double {
        val rootPlayerIndex = parent?.gameState?.activePlayer ?: gameState.activePlayer
        val rootPlayer = gameState.players[rootPlayerIndex]

        val opponent = gameState.players.first { it != rootPlayer }

        // Higher score is better. The score is the difference in remaining tokens.
        // A positive score means the root player won.
        return (opponent.tokenCount - rootPlayer.tokenCount).toDouble()
    }
    
    private fun selectBetterRolloutMove(gameState: NovaLunaGame, moves: List<Move>): Move {
        val currentPlayer = gameState.players[gameState.activePlayer]

        // Endgame Priority: If 5 or fewer tokens left, overwhelmingly prioritize task completion efficiency.
        if (currentPlayer.tokenCount <= 5) {
            val winningMoves = moves.mapNotNull { move ->
                val taskScore = evaluateQuickTaskCompletion(currentPlayer, move)
                if (taskScore > 0) {
                    val efficiency = taskScore / (move.tile?.time?.plus(1) ?: 1) // Higher is better
                    Pair(move, efficiency)
                } else {
                    null
                }
            }
            if (winningMoves.isNotEmpty()) {
                return winningMoves.maxByOrNull { it.second }?.first ?: moves.random()
            }
        }

        // Standard Rollout: Use a heuristic combining efficiency and position.
        return moves.maxByOrNull { move ->
            var score = 0.0
            val tile = move.tile ?: return@maxByOrNull 0.0

            // Efficiency Score: (Task Progress / Time Cost)
            val taskScore = evaluateQuickTaskCompletion(currentPlayer, move)
            val efficiency = if (tile.time > 0) taskScore / tile.time else taskScore * 2 // Avoid division by zero
            score += efficiency * 50

            // Positional Score: Simple clustering is good.
            val adjacentCount = countAdjacentTiles(currentPlayer, move.position)
            score += adjacentCount * 10

            // Time Score: Lower time is generally better.
            score += (10 - tile.time) * 5

            score
        } ?: moves.random()
    }
    
    private fun evaluateQuickTaskCompletion(player: Player, move: Move): Double {
        val tile = move.tile ?: return 0.0
        val position = move.position
        var completionScore = 0.0
        
        val adjacentColors = mutableMapOf<TileColour, Int>()
        val neighbors = getNeighborPositions(position)
        
        for (neighbor in neighbors) {
            val adjacentTile = player.tiles.find { it?.position == neighbor }
            if (adjacentTile != null) {
                val color = adjacentTile.tileColour
                adjacentColors[color] = adjacentColors.getOrDefault(color, 0) + 1
            }
        }
        
        for ((requirements, isCompleted) in tile.tasks) {
            if (isCompleted) continue
            
            var canComplete = true
            for ((requiredColor, requiredCount) in requirements) {
                if (adjacentColors.getOrDefault(requiredColor, 0) < requiredCount) {
                    canComplete = false
                    break
                }
            }
            
            if (canComplete) {
                completionScore += 20.0
            } else {
                val progress = requirements.entries.sumOf { (color, count) ->
                    minOf(adjacentColors.getOrDefault(color, 0), count)
                }.toDouble() / requirements.values.sum()
                completionScore += progress * 5.0
            }
        }
        
        return completionScore
    }

    /**
     * Backpropagates the score of a simulation up the tree.
     *
     * @param score The score of the simulation.
     */
    fun backpropagate(score: Double) {
        var currentNode: MCTSNode? = this
        while (currentNode != null) {
            currentNode.visits++
            // The score is from the perspective of the player who made the move leading to this node's parent.
            // We need to add it to that player's score.
            val playerIndex = currentNode.parent?.gameState?.activePlayer ?: currentNode.gameState.activePlayer
            currentNode.scores.merge(playerIndex, score, Double::plus)
            currentNode = currentNode.parent
        }
    }

    /**
     * Checks if this node is fully expanded.
     *
     * @return True if this node is fully expanded, false otherwise.
     */
    fun isFullyExpanded(): Boolean = untriedMoves.isEmpty()

    /**
     * Checks if the given game state is a terminal state.
     *
     * @param gameState The game state to check.
     * @return True if the game state is a terminal state, false otherwise.
     */
    fun isTerminal(gameState: NovaLunaGame): Boolean {
        if (gameState.players.any { it.tokenCount <= 0 }) {
            return true
        }
        
        val tilesAvailable = gameState.tileTrack.any { it != null }
        if (!tilesAvailable && gameState.drawPile.isEmpty()) {
            return true
        }
        
        val possibleMoves = getPossibleMoves(gameState)
        return possibleMoves.isEmpty()
    }

    private fun getPossibleMoves(gameState: NovaLunaGame): List<Move> {
        val availableTileIndices = mutableListOf<Int>()
        var position = gameState.meeplePosition
        var checked = 0
        
        while (availableTileIndices.size < 3 && checked < gameState.tileTrack.size - 1) {
            position = (position + 1) % gameState.tileTrack.size
            if (gameState.tileTrack[position] != null) {
                availableTileIndices.add(position)
            }
            checked++
        }
        
        val availableTiles = availableTileIndices.map { gameState.tileTrack[it] }.filterNotNull()
        
        val possiblePositions = getPossiblePositions(gameState)
        
        val moves = mutableListOf<Move>()
        for (tile in availableTiles) {
            for (position in possiblePositions) {
                moves.add(Move(tile, position))
            }
        }
        
        return moves
    }

    
    
    

    private fun uct(node: MCTSNode, explorationConstant: Double): Double {
        if (node.visits == 0) return Double.MAX_VALUE
        
        // Get the current player who is making the decision (from THIS node's perspective)
        val currentPlayer = gameState.activePlayer
        val wins = node.scores[currentPlayer] ?: 0.0
        val exploitation = wins / node.visits.toDouble()

        // Slightly favor exploitation over exploration
        val exploration = explorationConstant * sqrt(ln(visits.toDouble()) / node.visits.toDouble()) * 0.9 
        return exploitation + exploration
    }
    
    private fun applyMove(gameState: NovaLunaGame, move: Move) {
        val tile = move.tile ?: return
        val position = move.position
        
        val tileIndex = gameState.tileTrack.indexOf(tile)
        if (tileIndex != -1) {
            gameState.tileTrack[tileIndex] = null
        }
        
        val currentPlayer = gameState.players[gameState.activePlayer]
        val tileCopy = tile.copy(position = position, moonTrackPosition = null)
        currentPlayer.tiles.add(tileCopy)
        
        gameState.meeplePosition = tileIndex
        
        currentPlayer.moonTrackPosition += tile.time
        
        updatePlayerHeight(gameState, currentPlayer)
        
        simulateTaskCompletion(currentPlayer, tileCopy)
        
        refillTileTrackIfNeeded(gameState)
        
        findNextActivePlayer(gameState)
    }
    
    private fun updatePlayerHeight(gameState: NovaLunaGame, currentPlayer: Player) {
        currentPlayer.height = 0
        for (player in gameState.players) {
            if (player != currentPlayer && player.moonTrackPosition == currentPlayer.moonTrackPosition) {
                currentPlayer.height++
            }
        }
    }
    
    private fun refillTileTrackIfNeeded(gameState: NovaLunaGame) {
        val tilesInTrack = gameState.tileTrack.count { it != null }
        if (tilesInTrack <= 2 && gameState.drawPile.isNotEmpty()) {
            var index = (gameState.meeplePosition + 1) % gameState.tileTrack.size
            repeat(gameState.tileTrack.size - 1) {
                if (gameState.tileTrack[index] == null && gameState.drawPile.isNotEmpty()) {
                    gameState.tileTrack[index] = gameState.drawPile.removeAt(0)
                }
                index = (index + 1) % gameState.tileTrack.size
            }
        }
    }
    
    private fun findNextActivePlayer(gameState: NovaLunaGame) {
        var nextPlayer = gameState.players[0]
        for (player in gameState.players) {
            if (player.moonTrackPosition < nextPlayer.moonTrackPosition) {
                nextPlayer = player
            } else if (player.moonTrackPosition == nextPlayer.moonTrackPosition) {
                if (player.height > nextPlayer.height) {
                    nextPlayer = player
                }
            }
        }
        gameState.activePlayer = gameState.players.indexOf(nextPlayer)
    }
    
    private fun getPossiblePositions(gameState: NovaLunaGame): List<SerializableCoordinate> {
        val player = gameState.players[gameState.activePlayer]
        
        val occupied = player.tiles.mapNotNull { it?.position }.toSet()
        
        if (occupied.isEmpty()) {
            return listOf(SerializableCoordinate(0.0, 0.0))
        }
        
        val possible = mutableSetOf<SerializableCoordinate>()
        for (pos in occupied) {
            val neighbors = getNeighborPositions(pos)
            for (neighbor in neighbors) {
                if (neighbor !in occupied) {
                    possible.add(neighbor)
                }
            }
        }
        
        return possible.toList()
    }
    
    private fun simulateTaskCompletion(player: Player, newTile: Tile) {
        if (newTile.position == null) return

        var newlyCompletedTasks = 0

        val simulatedTaskStatus = mutableMapOf<Int, MutableList<Boolean>>()
        player.tiles.forEach { tile ->
            if (tile != null) {
                simulatedTaskStatus[tile.id] = tile.tasks.map { it.second }.toMutableList()
            }
        }

        for (tile in player.tiles) {
            if (tile == null || tile.position == null) continue

            for (i in tile.tasks.indices) {
                val wasCompleted = simulatedTaskStatus[tile.id]?.get(i) ?: false
                if (wasCompleted) continue

                val requirements = tile.tasks[i].first
                var allRequirementsMet = true
                for ((requiredColor, requiredCount) in requirements) {
                    val connectedColorTiles = findConnectedColorGroup(player, tile, requiredColor)
                    if (connectedColorTiles < requiredCount) {
                        allRequirementsMet = false
                        break
                    }
                }

                if (allRequirementsMet) {
                    newlyCompletedTasks++
                    simulatedTaskStatus[tile.id]?.set(i, true)
                }
            }
        }

        player.tokenCount = maxOf(0, player.tokenCount - newlyCompletedTasks)
    }

    private fun findConnectedColorGroup(player: Player, startTile: Tile, color: TileColour): Int {
        val neighbors = getNeighborPositions(startTile.position!!)
        val visited = mutableSetOf<SerializableCoordinate>()
        var count = 0

        for (neighborPos in neighbors) {
            val neighborTile = player.tiles.find { it?.position == neighborPos }
            if (neighborTile != null && neighborTile.tileColour == color && neighborPos !in visited) {
                val group = mutableSetOf<SerializableCoordinate>()
                val stack = ArrayDeque<SerializableCoordinate>()

                stack.add(neighborPos)
                group.add(neighborPos)
                visited.add(neighborPos)

                while (stack.isNotEmpty()) {
                    val currentPos = stack.removeLast()
                    val currentNeighbors = getNeighborPositions(currentPos)

                    for (currentNeighborPos in currentNeighbors) {
                        val currentNeighborTile = player.tiles.find { it?.position == currentNeighborPos }
                        if (currentNeighborTile != null && currentNeighborTile.tileColour == color && 
                            currentNeighborPos !in group
                        ) {
                            group.add(currentNeighborPos)
                            visited.add(currentNeighborPos)
                            stack.add(currentNeighborPos)
                        }
                    }
                }
                count += group.size
            }
        }
        return count
    }
    
    private fun getNeighborPositions(position: SerializableCoordinate): List<SerializableCoordinate> {
        return listOf(
            SerializableCoordinate(position.x + 1, position.y),
            SerializableCoordinate(position.x - 1, position.y),
            SerializableCoordinate(position.x, position.y + 1),
            SerializableCoordinate(position.x, position.y - 1)
        )
    }
}