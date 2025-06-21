package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual

class JoinGameSceneOne (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    val joinGameSceneTwo = JoinGameSceneTwo(rootService)

    /**
     * Container pane for centering all UI components.
     */
    private val contentPane = Pane<UIComponent>(
        width = 600,
        height = 500,
        posX = 660,
        posY = 290,
    )

    private val sessionInput = TextField(
        prompt = "Session ID",
        width = 400,
        height = 60,
        posX = 100,
        posY = 20,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0x49585D))
    )

    /**
     * Text field for entering Player name.
     */
    private val playerInput = TextField(
        prompt = "Player Name",
        width = 400,
        height = 60,
        posX = 100,
        posY = 100,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0x49585D))
    )

    private val urlInput = TextField(
        prompt = "URL",
        width = 400,
        height = 60,
        posX = 100,
        posY = 180,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0x49585D))
    )

    val easyButton = Button(
        text = "easy",
        width = 90,
        height = 50,
        posX = 200,
        posY = 260,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )
    val hardButton = Button(
        text = "hard",
        width = 90,
        height = 50,
        posX = 306,
        posY = 260,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )

    val joinButton = Button(
        text = "join",
        width = 200,
        height = 50,
        posX = 200,
        posY = 340,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0xd53032))
    )

    val backButton = Button(
        text = "back",
        width = 200,
        height = 50,
        posX = 200,
        posY = 410,
        font = Font(18, Color.WHITE),
        visual = ColorVisual(Color(0x48a43f))
    )

    init {
        // Set the background image for the main menu
        background = ImageVisual("OfflineMenu.png")

        addComponents(contentPane)
        contentPane.addAll( backButton, joinButton, sessionInput, playerInput, urlInput, easyButton, hardButton)
    }
}