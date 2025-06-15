package gui

import service.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.components.layoutviews.*
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.util.*
import tools.aqua.bgw.visual.*

/**
 *
 * Implements [Refreshable] to react to service layer updates.
 *
 * @param rootService Provides access to GameService for starting new game.
 */
class MainMenuScene (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {


    val offlineScene = OfflineScene(rootService)

    /**
     * Container pane for centering all UI components.
     */
    private val contentPane = Pane<UIComponent>(
        width = 800,
        height = 400,
        posX = 50,
        posY = 225,
    )

    /**
     * Button to quit the application immediately.
     */
    val quitButton = Button(
        text = "Quit",
        width = 200,
        height = 50,
        posX = 800,
        posY = 600,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )
    val offlineButton = Button(
        text = "Offline",
        width = 200,
        height = 50,
        posX = 800,
        posY = 300,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )

    val hostButton = Button(
        text = "Host Game",
        width = 200,
        height = 50,
        posX = 800,
        posY = 360,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )

    val joinButton = Button(
        text = "Join Game",
        width = 200,
        height = 50,
        posX = 800,
        posY = 420,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )

    /**
     * Button to start a new game with the entered player names.
     * Validates that both names are non‑empty and distinct.
     */
    private val loadButton = Button(
        text = "Load Game",
        width = 200, height = 50,
        posX = 800, posY = 540,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0x48a43f))
    )


    init {
        // Set the background image for the main menu
        background = ImageVisual("MainMenu.png")

        addComponents(contentPane)
        contentPane.addAll( loadButton, quitButton, offlineButton, hostButton, joinButton)
    }


    override fun refreshAfterStartTurn() {
        // Wird in MainMenuScene nicht benötigt
    }
}