package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.core.MenuScene

class HostGameSceneTwo (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {
    //this needs to be called from the start button in the hostgame lobby (I am assuming it's similar to the offline lobby scene)
    //rootService.networkService.startNewHostedGame(
    //playersStartGame,
    //ifFirstGame,
   // ifRandom
    //)
}