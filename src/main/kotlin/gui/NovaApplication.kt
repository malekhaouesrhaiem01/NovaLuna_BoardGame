package gui

import entity.Player
import tools.aqua.bgw.core.BoardGameApplication
import service.Refreshable
import service.RootService

/**
 * Implementation of the BGW [BoardGameApplication] for the game "NovaLuna"
 */
class NovaApplication : BoardGameApplication("NovaLuna"), Refreshable {

    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

    // Scenes

    // This menu scene is shown after application start and if the "new game" button
    // is clicked in the gameFinishedMenuScene
    private val mainMenuScene = MainMenuScene(rootService).apply {
        quitButton.onMouseClicked = {
            exit()
        }

        offlineButton.onMouseClicked = {
            showMenuScene(offlineScene)
        }
    }

    private val offlineScene = OfflineScene(rootService).apply {

    }

    init {
        // Register all components that need refresh callbacks
        rootService.addRefreshables(
            this,
            mainMenuScene,
            offlineScene
        )

        // Show the game scene first, then overlay the main menu
        this.showMenuScene(mainMenuScene, 0)

    }


    override fun refreshAfterStartTurn() {

    }

}