package gui

import service.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.components.layoutviews.*
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.*
import tools.aqua.bgw.visual.*

/**
 *
 * Implements [Refreshable] to react to service layer updates.
 *
 */
class MainMenuScene () : MenuScene(1920, 1080,ImageVisual("back_image.png")), Refreshable {


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
            style.borderRadius = BorderRadius(30)
        }
    )

    /**
     * Button to quit the application immediately.
     */
    private val loadButton = Button(
        text = "Load Game",
        width = 326,
        height = 100,
        posX = 798,
        posY = 740,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0x8B570C)).apply {
            style.borderRadius = BorderRadius(30)
        }
    )

    private val exitButton = Button(
        text = "Exit",
        width = 326,
        height = 100,
        posX = 798,
        posY = 859,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0x331F01)).apply {
            style.borderRadius = BorderRadius(30)
        }
    ).apply {
        onMouseClicked = {
            NovaApplication.exit()
        }
    }
    val offlineButton = Button(
        text = "Offline",
        width = 326,
        height = 100,
        posX = 799,
        posY = 267,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(30)
        }
    )

    val hostButton = Button(
        text = "Host Game",
        width = 326,
        height = 100,
        posX = 799,
        posY = 408,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(30)
        }
    )

    val joinButton = Button(
        text = "Join Game",
        width = 326,
        height = 100,
        posX = 798,
        posY = 549,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(30)
        }
    )


    init {
        //contentPane.addAll(exitButton)

        contentPane.addAll(labelNovaLuna, backToken, offlineButton, hostButton, joinButton, loadButton, exitButton)
        addComponents(contentPane)

    }
}