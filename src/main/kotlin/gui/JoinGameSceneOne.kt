package gui

import service.*
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import entity.PlayerType
import java.util.*

class JoinGameSceneOne (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

    //val joinGameSceneTwo = JoinGameSceneTwo(rootService)

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
    val playerName = playerInput.text

    private val urlInput = TextField(
        prompt = "URL",
        width = 400,
        height = 90,
        posX = 760,
        posY = 487,
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

    val joinButton = Button(
        text = "join",
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

        joinButton.onMouseClicked = onMouseClicked@ {
            val name      = playerInput.text
            val sessionId = sessionInput.text
            val secret    = "neumond25"

            if (name.isEmpty()) {
                showError("Please enter a name!")
                return@onMouseClicked
            }
            if (sessionId.isEmpty()) {
                showError("Please enter a session ID!")
                return@onMouseClicked
            }
            if (urlInput.text.isEmpty()) {
                showError("Please enter a URL!")
                return@onMouseClicked
            }

            rootService.networkService.myPlayerType = when (whichPlayer) {
                1    -> PlayerType.EASYBOT
                2    -> PlayerType.HARDBOT
                else -> PlayerType.HUMAN
            }

            // 2) Perform the network join
            rootService.networkService.joinGame(
                secret = secret,
                name = name,
                sessionID  = sessionId
            )



            val joinGameSceneTwo = JoinGameSceneTwo(rootService, name)
            NovaApplication.showMenuScene(joinGameSceneTwo)
        }

        addComponents(contentPane)
        contentPane.addAll(backToken, backButton, joinButton,
            sessionInput, playerInput, urlInput, easyButton,
            hardButton, labelNovaLuna, errorLabel)
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