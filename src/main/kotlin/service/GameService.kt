package service

import entity.NovaLunaGame
import entity.Player

class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    fun startNewGame(players : List<Player>, simulationSpeed : Int) {

        // überprüfe, ob Anzahl der Spieler passt (2 bis 4)
        require(players.size in 2..4) { "Spieleranzahl muss zwischen 2 und 4 sein." }

        require(simulationSpeed < 11) { "SimulationSpeed darf maximal 10 sein" }

        // Initializing the draw pile
        val drawPile = rootService.tileLoader.readTiles().shuffled().toMutableList()

        // Initializing the tileTrack with the top 11 tiles from the drawPile
        val tileTrack = drawPile.subList(0, 11)
        drawPile.subList(0, 11).clear()

        // setting heights according to beginning order
        players[0].height <- 4
        players[1].height <- 3
        players[2].height <- 2
        players[3].height <- 1

        val game = NovaLunaGame(
            activePlayer = 0,
            meeplePosition = 0,
            simulationSpeed = simulationSpeed,
            players = players.toMutableList(),
            drawPile = drawPile,
            tileTrack = tileTrack
        )

        rootService.currentGame = game

    }

}