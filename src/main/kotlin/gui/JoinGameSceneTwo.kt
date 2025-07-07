package gui

import service.*
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*


/**
 *  The Waiting Lobby when joining an online Game in NovaLuna
 *
 *  Implements [Refreshable] to react to service layer updates.
 *
 *  @constructor Creates a new Waiting Lobby Scene with the specified rootService and Player Name.
 *
 *  @param rootService The [RootService] that manages the game state.
 *  @param playerName The Name of the Player joining
 */
class JoinGameSceneTwo (private val rootService: RootService,
                        private val playerName: String) : MenuScene(1920, 1080), Refreshable {

    /**
     * Container pane for centering all UI components.
     */
    private val contentPane = Pane<UIComponent>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ImageVisual("back_image.png")
    )

    private val nameLabel = Label(
        text = "Welcome, $playerName!",
        width = 600,
        height = 100,
        posX = 660,
        posY = 330,
        alignment = Alignment.CENTER,
        font = Font(48, Color.BLACK, "Space Grotesk"),
        visual = ColorVisual(Color(0xFFD28E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val waitLabel = Label(
        text = "Wait for others",
        width = 600,
        height = 100,
        posX = 660,
        posY = 450,
        alignment = Alignment.CENTER,
        font = Font(48, Color.BLACK, "Space Grotesk"),
        visual = ColorVisual(Color(0xEEC07E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    val exitButton = Button(
        text = "Exit",
        width = 326,
        height = 100,
        posX = 797,
        posY = 600,
        font = Font(48, Color.WHITE, "Space Grotesk"),
        visual = ColorVisual(Color(0x331F01)).apply {
            style.borderRadius = BorderRadius(30)
        }
    ).apply {
        onMouseClicked = {
            NovaApplication.exit()
        }
    }

    init {

        addComponents(contentPane)
        contentPane.addAll(nameLabel, waitLabel, exitButton)
    }

}