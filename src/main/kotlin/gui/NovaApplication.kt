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

    private lateinit var joinGameSceneOne: JoinGameSceneOne
    private lateinit var joinGameSceneTwo: JoinGameSceneTwo
    private lateinit var hostGameSceneOne: HostGameSceneOne
    private lateinit var hostGameSceneTwo: HostGameSceneTwo

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

        joinButton.onMouseClicked = {
            showMenuScene(joinGameSceneOne)
        }

        hostButton.onMouseClicked = {
            showMenuScene(hostGameSceneOne)
        }
    }

    private val offlineScene = OfflineScene(rootService).apply {

    }



    init {

        joinGameSceneOne = JoinGameSceneOne(rootService).apply {
            joinButton.onMouseClicked = {
                showMenuScene(joinGameSceneTwo)
            }

            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }

        joinGameSceneTwo = JoinGameSceneTwo(rootService)

        hostGameSceneOne = HostGameSceneOne(rootService).apply {
            nextButton.onMouseClicked = {
                showMenuScene(hostGameSceneTwo)
            }

            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }

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