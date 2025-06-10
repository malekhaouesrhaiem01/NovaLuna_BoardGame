package service

import entity.Player

class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    fun startNewGame(players : List<Player>, simulationSpeed : Int) {

        // überprüfe, ob Anzahl der Spieler passt (2 bis 4)
        require(players.size in 2..4) { "Spieleranzahl muss zwischen 2 und 4 sein." }


    }

}