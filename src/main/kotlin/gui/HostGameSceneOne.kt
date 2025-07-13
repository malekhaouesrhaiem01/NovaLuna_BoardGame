package gui

import service.*
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import entity.PlayerType
import java.util.*
/**
 *  The HostGame Scene for an online Game in NovaLuna.
 *
 *  Implements [Refreshable] to react to service layer updates.
 *
 *  @constructor Creates a new HostGame Scene with the specified rootService.
 *
 *  @param rootService The [RootService] that manages the game state.
 *  @property hostGameSceneTwo The Game Config Scene when hosting a Game
 */
class HostGameSceneOne (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    val hostGameSceneTwo = HostGameSceneTwo(rootService)

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

    private val labelNovaLuna = Label(
        posX = 706,
        posY = 62,
        width = 509,
        height = 94,
        text = "Nova Luna",
        alignment = Alignment.TOP_CENTER,
        font = Font(96, Color(0x000000), "Aldrich")
    )

    private val backToken = Label(
        posX = 684,
        posY = 217,
        width = 553,
        height = 785,
        visual = ColorVisual(Color(0xEEC07E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    val visualOff = ColorVisual(Color(193, 120, 12)).apply {
        style.borderRadius = BorderRadius(15)
        transparency = 0.5
    }
    val visualOn = ColorVisual(Color.GREEN).apply {
        style.borderRadius = BorderRadius(15)
        transparency = 0.5
    }

    var whichPlayer = 0 // 0 = human, 1 = easy, 2 = hard

    private val sessionInput = TextField(
        prompt = "Session ID",
        width = 400,
        height = 90,
        posX = 760,
        posY = 267,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    /**
     * Text field for entering Player name.
     */
    private val playerInput = TextField(
        prompt = "Player Name",
        width = 400,
        height = 90,
        posX = 760,
        posY = 377,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    val easyButton = Button(
        text = "easy",
        width = 180,
        height = 90,
        posX = 760,
        posY = 607,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )
    val hardButton = Button(
        text = "hard",
        width = 180,
        height = 90,
        posX = 975,
        posY = 607,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    val nextButton = Button(
        text = "next",
        width = 400,
        height = 90,
        posX = 760,
        posY = 727,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    val backButton = Button(
        text = "back",
        width = 400,
        height = 90,
        posX = 760,
        posY = 847,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val errorLabel = Label(
        text = "",
        width = 800,
        height = 80,
        posX = width / 2 - 400,
        posY = 600,
        font = Font(50, Color.RED, "Space Grotesk"),
        visual = ColorVisual(Color(0xFFF0F0)).apply { style.borderRadius = BorderRadius(10) }
    ).apply { isVisible = false }

    init {
        // Set the background image for the main menu
        background = ImageVisual("OfflineMenu.png")
        easyButton.onMouseClicked = {
            when (whichPlayer) {
                1 -> {
                    easyButton.visual = visualOff
                    whichPlayer = 0
                }
                else -> {
                    easyButton.visual = visualOn
                    hardButton.visual = visualOff
                    whichPlayer = 1
                }
            }
        }

        hardButton.onMouseClicked = {
            when (whichPlayer) {
                2 -> {
                    hardButton.visual = visualOff
                    whichPlayer = 0
                }
                else -> {
                    hardButton.visual = visualOn
                    easyButton.visual = visualOff
                    whichPlayer = 2
                }
            }
        }

        nextButton.onMouseClicked = onMouseClicked@ {
            val name      = playerInput.text
            val sessionID = sessionInput.text
            val secret    = "neumond25"  // add a TextField if you need a separate secret field

            if (name.isBlank()) {
                showError("Please enter a host name!")
                return@onMouseClicked
            }

            // 1) Record host’s player type (human/easy/hard) in the service
            rootService.networkService.myPlayerType = when (whichPlayer) {
                1    -> PlayerType.EASYBOT
                2    -> PlayerType.HARDBOT
                else -> PlayerType.HUMAN
            }

            // 3) Tell the service to host a game on the server
            rootService.networkService.hostGame(
                secret     = secret,
                playerName = name,
                sessionID  = sessionID
            )

            // 4) Navigate to the lobby scene where you wait for guests
            NovaApplication.showMenuScene(hostGameSceneTwo)
        }

        addComponents(contentPane)
        contentPane.addAll( backToken, nextButton, sessionInput,
            playerInput, easyButton, hardButton,
            labelNovaLuna, backButton, errorLabel)
    }

    private fun showError(message: String) {
        errorLabel.text = message
        errorLabel.isVisible = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                errorLabel.isVisible = false
            }
        }, 3000)
    }
}