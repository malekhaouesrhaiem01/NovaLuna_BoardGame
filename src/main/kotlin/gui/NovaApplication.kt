package gui

import tools.aqua.bgw.core.BoardGameApplication
import service.Refreshable
import service.RootService

/**
 * Implementation of the BGW [BoardGameApplication] for the game "NovaLuna"
 */
object NovaApplication : BoardGameApplication("NovaLuna"), Refreshable {

    private val rootService = RootService()

    private var mainMenuScene: MainMenuScene
    private lateinit var offlineMenuScene: OfflineMenuScene

    private val gameScene = GameScene(rootService)

    init {
        // Register all components that need refresh callbacks

        mainMenuScene = MainMenuScene(rootService).apply {
            offlineButton.onMouseClicked = {
                showMenuScene(offlineMenuScene)
            }
        }
        offlineMenuScene = OfflineMenuScene(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }

        rootService.addRefreshables(
            this,
            mainMenuScene,
            offlineMenuScene,
            gameScene
        )

        this.showGameScene(gameScene)
        this.showMenuScene(mainMenuScene)

    }

    override fun refreshAfterStartGame() {
        hideMenuScene(500)
    }

}