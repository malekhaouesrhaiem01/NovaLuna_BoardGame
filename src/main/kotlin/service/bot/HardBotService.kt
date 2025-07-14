package service.bot

import entity.Move
import service.ConnectionState
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

        val isNetworkGame = rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN

        val delay = when (game.simulationSpeed) {
            0 -> 0L
            1 -> 1000L
            3 -> 3000L
            else -> 1000L // Default delay
        }

        if (isNetworkGame) {
            // For network games: simpler flow, no automatic endTurn()
            Thread {
                val bestMove = findBestMove(game, possibleMoves, System.currentTimeMillis() + 3500)

                // Use minimal delay for network games to avoid timing issues
                val networkDelay = if (delay == 0L) 0L else 500L

                scheduler.schedule({
                    println("HardBot playing: ${bestMove.tile?.id} at ${bestMove.position}")
                    // Only play the tile, network service will handle endTurn()
                    rootService.playerActionService.playTile(bestMove)
                }, networkDelay, TimeUnit.MILLISECONDS)
            }.start()
        } else {
            // For offline games: keep the original behavior
            Thread {
                val bestMove = findBestMove(game, possibleMoves, System.currentTimeMillis() + 6500)
                scheduler.schedule({
                    println("HardBot playing: ${bestMove.tile?.id} at ${bestMove.position}")
                    rootService.playerActionService.playTile(bestMove)
                    scheduler.schedule({
                        rootService.gameService.endTurn()
                    }, 1, TimeUnit.SECONDS)
                }, delay, TimeUnit.MILLISECONDS)
            }.start()
        }
    }

    /**
     * Finds the best move for the hard bot using MCTS.
     *
     * This method creates a root node for the MCTS, runs a number of simulations to find the best move, and then
     * returns the best move.
     *
     * @param game The current game state.
     * @param moves The list of possible moves.
     * @param endTime The time when the search should stop.
     * @return The best move found by the MCTS.
     */
    private fun findBestMove(game: entity.NovaLunaGame, moves: List<Move>, endTime: Long): Move {
        if (moves.size == 1) {
            return moves[0] // Only one move available, no need for MCTS
        }
        
        // Quick heuristic evaluation if we have limited time
        val timeAvailable = endTime - System.currentTimeMillis()
        if (timeAvailable < 1000) { // Less than 1 second, use quick heuristic
            return selectBestMoveHeuristic(game, moves)
        }
        
        val root = MCTSNode(game.deepCopy(), untriedMoves = moves.toMutableList())
        
        var iterations = 0
        var lastLogTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() < endTime) {
            // Selection: traverse the tree using UCT
            var node = root
            while (node.untriedMoves.isEmpty() && node.children.isNotEmpty()) {
                node = node.selectBestChild() ?: break
            }
            
            // Expansion: if node has untried moves, expand it
            if (node.untriedMoves.isNotEmpty()) {
                node = node.expand()
            }
            
            // Simulation: run random simulation from this node
            val score = node.rollout()
            
            // Backpropagation: update statistics
            node.backpropagate(score)
            
            iterations++
            
            // Log progress every 3 seconds
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLogTime > 3000) {
                println("MCTS: $iterations iterations completed, ${(endTime - currentTime)/1000.0}s remaining")
                if (root.children.isNotEmpty()) {
                    val bestChild = root.children.maxByOrNull { it.visits }
                    println("Current best: ${bestChild?.moveThatLedHere} with ${bestChild?.visits} visits")
                }
                lastLogTime = currentTime
            }
            
            // Early exit if we've done a lot of iterations (performance optimization)
            if (iterations > 100000) {
                println("MCTS: Early exit after $iterations iterations")
                break
            }
            
            // Early exit if we have a very confident choice
            if (iterations > 5000 && root.children.isNotEmpty()) {
                val bestChild = root.children.maxByOrNull { it.visits }
                val totalVisits = root.children.sumOf { it.visits }
                if (bestChild != null && bestChild.visits > totalVisits * 0.8) {
                    println("MCTS: Early exit with confident choice after $iterations iterations")
                    break
                }
            }
        }
        
        println("MCTS completed with $iterations iterations")
        
        // Return the move of the most visited child (most robust choice)
        val bestChild = root.children.maxByOrNull { it.visits }
        if (bestChild != null) {
            val winRate = (bestChild.scores[game.activePlayer] ?: 0.0) / bestChild.visits
            println("Best move: ${bestChild.moveThatLedHere} with ${bestChild.visits} visits (${String.format("%.2f", winRate * 100)}% win rate)")
            return bestChild.moveThatLedHere!!
        }
        
        // Fallback to heuristic if MCTS failed
        println("MCTS failed, falling back to heuristic")
        return selectBestMoveHeuristic(game, moves)
    }
    
    /**
     * Selects the best move using smart rule-based heuristics.
     * This uses Nova Luna strategic principles rather than generic adjacency.
     */
    private fun selectBestMoveHeuristic(game: entity.NovaLunaGame, moves: List<Move>): Move {
        val currentPlayer = game.players[game.activePlayer]
        
        return moves.maxByOrNull { move ->
            var score = 0.0
            
            // 1. ACTUAL TASK COMPLETION (highest priority)
            val taskScore = calculateActualTaskValue(currentPlayer, move)
            score += taskScore * 100
            
            // 2. EFFICIENT TILE SELECTION (time vs benefit)
            val efficiencyScore = calculateTileEfficiency(move)
            score += efficiencyScore * 50
            
            // 3. STRATEGIC POSITIONING (hexagonal clustering, not lines)
            val positionScore = calculateStrategicPositioning(currentPlayer, move)
            score += positionScore * 30
            
            // 4. GAME PHASE ADAPTATION
            val phaseScore = calculateGamePhaseBonus(currentPlayer, move)
            score += phaseScore * 20
            
            // 5. Minimal randomness for tie-breaking
            score += (Math.random() - 0.5) * 1
            
            score
        } ?: moves.random()
    }
    
    /**
     * Calculates the actual task completion value based on Nova Luna rules.
     */
    private fun calculateActualTaskValue(player: entity.Player, move: Move): Double {
        val tile = move.tile ?: return 0.0
        val position = move.position
        var taskValue = 0.0
        
        // Get current color distribution around the position
        val colorMap = getColorDistributionAroundPosition(player, position)
        
        // Evaluate each task on the tile being placed
        for ((requirements, isCompleted) in tile.tasks) {
            if (isCompleted) continue
            
            var immediateCompletion = true
            var progressValue = 0.0
            var totalRequirements = 0
            
            for ((requiredColor, requiredCount) in requirements) {
                val available = colorMap.getOrDefault(requiredColor, 0)
                totalRequirements += requiredCount
                
                if (available >= requiredCount) {
                    progressValue += requiredCount.toDouble()
                } else {
                    immediateCompletion = false
                    progressValue += available.toDouble()
                }
            }
            
            if (immediateCompletion) {
                // Immediate task completion is extremely valuable
                taskValue += 50.0
                // Bonus for multiple requirement tasks
                taskValue += (totalRequirements - 1) * 10.0
            } else {
                // Progress towards completion is valuable
                val completionRatio = if (totalRequirements > 0) progressValue / totalRequirements else 0.0
                taskValue += completionRatio * 15.0
            }
        }
        
        // Check if this placement helps complete tasks on existing tiles
        taskValue += calculateAdjacentTaskHelp(player, move, colorMap)
        
        return taskValue
    }
    
    /**
     * Calculates how this move helps complete tasks on adjacent existing tiles.
     */
    private fun calculateAdjacentTaskHelp(player: entity.Player, move: Move, colorMap: MutableMap<entity.TileColour, Int>): Double {
        val tile = move.tile ?: return 0.0
        val position = move.position
        var helpValue = 0.0
        
        // Add our tile's color to the map
        val colorMapWithNewTile = colorMap.toMutableMap()
        colorMapWithNewTile[tile.tileColour] = colorMapWithNewTile.getOrDefault(tile.tileColour, 0) + 1
        
        // Check each adjacent position for existing tiles
        val neighbors = getNeighborPositions(position)
        for (neighbor in neighbors) {
            val adjacentTile = player.tiles.find { it?.position == neighbor }
            if (adjacentTile != null) {
                // Get colors around the adjacent tile (including our new tile)
                val adjacentColorMap = getColorDistributionAroundPosition(player, neighbor)
                adjacentColorMap[tile.tileColour] = adjacentColorMap.getOrDefault(tile.tileColour, 0) + 1
                
                // Check if any tasks on the adjacent tile can now be completed
                for ((requirements, isCompleted) in adjacentTile.tasks) {
                    if (isCompleted) continue
                    
                    var canComplete = true
                    for ((requiredColor, requiredCount) in requirements) {
                        if (adjacentColorMap.getOrDefault(requiredColor, 0) < requiredCount) {
                            canComplete = false
                            break
                        }
                    }
                    
                    if (canComplete) {
                        helpValue += 30.0 // Very valuable to help complete adjacent tasks
                    }
                }
            }
        }
        
        return helpValue
    }
    
    /**
     * Calculates tile efficiency based on Nova Luna principles.
     */
    private fun calculateTileEfficiency(move: Move): Double {
        val tile = move.tile ?: return 0.0
        var efficiency = 0.0
        
        // Lower time cost is generally better (more turns)
        efficiency += (6 - tile.time) * 5.0
        
        // Tiles with more tasks have more potential
        efficiency += tile.tasks.size * 8.0
        
        // Tiles with complex tasks (multiple colors) are often more valuable
        val totalColorRequirements = tile.tasks.sumOf { (requirements, _) -> 
            requirements.values.sum()
        }
        efficiency += totalColorRequirements * 2.0
        
        return efficiency
    }
    
    /**
     * Calculates strategic positioning value focusing on hexagonal patterns, not lines.
     */
    private fun calculateStrategicPositioning(player: entity.Player, move: Move): Double {
        val position = move.position
        var positionValue = 0.0
        
        if (player.tiles.isEmpty()) {
            // First tile should be at origin - no preference for lines
            return if (position.x == 0.0 && position.y == 0.0) 20.0 else -10.0
        }
        
        // Count adjacent connections
        val neighbors = getNeighborPositions(position)
        val adjacentTiles = neighbors.count { neighbor ->
            player.tiles.any { it?.position == neighbor }
        }
        
        when (adjacentTiles) {
            0 -> positionValue -= 50.0 // Isolated placement is bad
            1 -> positionValue += 5.0   // Basic extension
            2 -> positionValue += 15.0  // Good - creates opportunities
            3 -> positionValue += 30.0  // Excellent - central hub position
            4 -> positionValue += 25.0  // Very good but maybe dense
            5 -> positionValue += 20.0  // Dense but still valuable
            6 -> positionValue += 10.0  // Maximum density - less future potential
        }
        
        // Bonus for creating "hub" positions that enable future clustering
        val hubPotential = calculateHubPotential(player, position)
        positionValue += hubPotential * 5.0
        
        return positionValue
    }
    
    /**
     * Calculates how much this position creates future connection opportunities.
     */
    private fun calculateHubPotential(player: entity.Player, position: entity.SerializableCoordinate): Double {
        val neighbors = getNeighborPositions(position)
        var hubValue = 0.0
        
        for (neighbor in neighbors) {
            // Skip if this neighbor is already occupied
            if (player.tiles.any { it?.position == neighbor }) continue
            
            // Check how many existing tiles this future position would connect to
            val futureNeighbors = getNeighborPositions(neighbor)
            val futureConnections = futureNeighbors.count { futureNeighbor ->
                player.tiles.any { it?.position == futureNeighbor }
            }
            
            // Positions that would connect to 2+ tiles are valuable hubs
            if (futureConnections >= 2) {
                hubValue += futureConnections.toDouble()
            }
        }
        
        return hubValue
    }
    
    /**
     * Calculates game phase bonuses.
     */
    private fun calculateGamePhaseBonus(player: entity.Player, move: Move): Double {
        val tile = move.tile ?: return 0.0
        var phaseBonus = 0.0
        
        when {
            // Early game (many tokens) - focus on setup and efficiency
            player.tokenCount > 15 -> {
                phaseBonus += (10 - tile.time) * 2.0 // Prefer efficient tiles
                phaseBonus += tile.tasks.size * 3.0  // Prefer tiles with potential
            }
            
            // Mid game (moderate tokens) - balance efficiency and completion
            player.tokenCount in 6..15 -> {
                phaseBonus += calculateActualTaskValue(player, move) * 0.5 // Moderate task focus
            }
            
            // End game (few tokens) - prioritize immediate task completion
            player.tokenCount <= 5 -> {
                phaseBonus += calculateActualTaskValue(player, move) * 1.5 // High task focus
                if (tile.time <= 2) phaseBonus += 10.0 // Prefer low-cost tiles for quick wins
            }
        }
        
        return phaseBonus
    }
    
    /**
     * Gets color distribution around a position.
     */
    private fun getColorDistributionAroundPosition(player: entity.Player, position: entity.SerializableCoordinate): MutableMap<entity.TileColour, Int> {
        val colorMap = mutableMapOf<entity.TileColour, Int>()
        val neighbors = getNeighborPositions(position)
        
        for (neighbor in neighbors) {
            val tile = player.tiles.find { it?.position == neighbor }
            if (tile != null) {
                colorMap[tile.tileColour] = colorMap.getOrDefault(tile.tileColour, 0) + 1
            }
        }
        
        return colorMap
    }
    
    /**
     * Gets neighbor positions for a coordinate.
     */
    private fun getNeighborPositions(position: entity.SerializableCoordinate): List<entity.SerializableCoordinate> {
        return listOf(
            entity.SerializableCoordinate(position.x + 1, position.y),
            entity.SerializableCoordinate(position.x - 1, position.y),
            entity.SerializableCoordinate(position.x, position.y + 1),
            entity.SerializableCoordinate(position.x, position.y - 1)
        )
    }
    
    /**
     * Counts adjacent tiles to a position for a player.
     */
    private fun countAdjacentTiles(player: entity.Player, position: entity.SerializableCoordinate): Int {
        val neighbors = getNeighborPositions(position)
        return neighbors.count { neighbor ->
            player.tiles.any { tile -> tile?.position == neighbor }
        }
    }
}