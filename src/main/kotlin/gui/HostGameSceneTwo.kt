package gui

import entity.*
import service.*
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import java.util.*


/**
 *  The GameConfig Scene when Hosting a Game in NovaLuna
 *
 *  Implements [Refreshable] to react to service layer updates.
 *
 *  @constructor Creates a new GameConfig Scene with the specified rootService.
 *
 *  @param rootService The [RootService] that manages the game state.
 *  @property availableColors A List of available colors the Player can still choose from
 *  @property ifRandom Boolean to determine if the Player order is Random or not
 *  @property ifFirstGame Boolean to determine if it's the Players FirstGame
 *  @property currentColorPickerIndex The Index of the current Color
 */

class HostGameSceneTwo (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {
    //this needs to be called from the start button in the hostgame lobby
    //(I am assuming it's similar to the offline lobby scene)
    //rootService.networkService.startNewHostedGame(
    //playersStartGame,
    //ifFirstGame,
    //ifRandom
    //)

    private val availableColors = mutableListOf(true, true, true, true)
    private var ifRandom = false
    private var ifFirstGame : Boolean = false
    private var currentColorPickerIndex = -1


    override fun refreshAfterPlayerJoined() {
        // always rebuild the list of player rows
        // and do it on the BGW-UI thread


            buildPlayerUIs()


    }
    private val contentPane = Pane<UIComponent>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ImageVisual("back_image.png")
    )

    private val orderToken = Label(
        posX = 134,
        posY = 112,
        width = 241,
        height = 75,
        text = "Order",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xF9B44E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val playersToken = Label(
        posX = 544.28,
        posY = 112,
        width = 352.72,
        height = 75,
        text = "Players",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xF9B44E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val colorsToken = Label(
        posX = 1042,
        posY = 112,
        width = 273.9,
        height = 75,
        text = "Colors",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xF9B44E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    //Pane to select a color
    private val selectColorPane = Pane<UIComponent>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ColorVisual(Color.BLACK).apply { transparency = 0.5 }
    ).apply { isVisible = false }

    private val colorSelectLabel = Label(
        posX = 754,
        posY = 273,
        width = 418,
        height = 532 + 110.45,
        visual = ColorVisual(Color(0xFDCF8D)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val noneColor = Button(
        text = "NONE",
        width = 273.9,
        height = 89.74,
        posX = 826,
        posY = 327,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color.GRAY).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val blackColor = Button(
        text = "Black",
        width = 273.9,
        height = 89.74,
        posX = 826,
        posY = 438.43,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color.BLACK).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val whiteColor = Button(
        text = "White",
        width = 273.9,
        height = 89.74,
        posX = 826,
        posY = 550.85,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color.WHITE).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val blueColor = Button(
        text = "Blue",
        width = 273.9,
        height = 89.74,
        posX = 826,
        posY = 661.3,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color.BLUE).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val orangeColor = Button(
        text = "Orange",
        width = 273.9,
        height = 89.74,
        posX = 826,
        posY = 771.75,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xFF8401)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    /**
     * A data class to display the different Labels of a Player in the GameConfig Scene
     * @property orderToken The Order of the Players
     * @property nameLabel Name of the Player
     * @property colorButton Button of the color
     * @property colorIndex Index of the Color
     */
    data class PlayerGUI(
        val orderToken: Label,
        val nameLabel: Label,
        val colorButton: Button,
        var colorIndex : Int = 4
    )
    private val playerGUIs = mutableListOf<PlayerGUI>()


    private val randomButton = Button(
        text = "random",
        width = 241,
        height = 75,
        posX = 134,
        posY = 893,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply {
        onMouseClicked = {
            if (ifRandom) {
                ifRandom = false
                visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                    style.borderRadius = BorderRadius(15)
                }
            }else{
                ifRandom = true
                visual = ColorVisual(Color(0x05D817)).apply {
                    style.borderRadius = BorderRadius(15); transparency = 0.54
                }
            }
        }
    }

    val backButton = Button(
        text = "back",
        width = 241,
        height = 75,
        posX = 544,
        posY = 893,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply {
        onMouseClicked = {

        }
    }


    private val firstGame = Button(
        text = "First Game",
        width = 289.4,
        height = 75,
        posX = 1105,
        posY = 893,
        font = Font(48, Color.BLACK, "Space Grotesk"),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply {
        onMouseClicked = {
            if (ifFirstGame) {
                ifFirstGame = false
                visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                    style.borderRadius = BorderRadius(15)
                }
            }else{
                ifFirstGame = true
                visual = ColorVisual(Color(0x05D817)).apply {
                    style.borderRadius = BorderRadius(15); transparency = 0.54
                }
            }
        }
    }

    private val errorLabel = Label(
        text = "",
        width = 800,
        height = 80,
        posX = width / 2 - 300,
        posY = 700,
        font = Font(50, Color.RED, "Space Grotesk"),
        visual = ColorVisual(Color(0xFFF0F0)).apply { style.borderRadius = BorderRadius(10) }
    ).apply { isVisible = false }

    private val startButton = Button(
        text = "Start",
        width = 241,
        height = 75,
        posX = 1439,
        posY = 893,
        font = Font(48, Color.BLACK, "Space Grotesk"),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    init {
        rootService.networkService.addRefreshable(this)

        selectColorPane.addAll(colorSelectLabel, noneColor, blackColor,
            whiteColor, blueColor, orangeColor)

        contentPane.addAll(orderToken, playersToken,
            colorsToken, randomButton, backButton, firstGame, startButton, errorLabel)

        addComponents(contentPane, selectColorPane)

        noneColor.onMouseClicked = { applyColor(4) }
        blackColor.onMouseClicked = { applyColor(0) }
        whiteColor.onMouseClicked = { applyColor(1) }
        blueColor.onMouseClicked = { applyColor(2) }
        orangeColor.onMouseClicked = { applyColor(3) }

        buildPlayerUIs()

        // Control buttons
        firstGame.onMouseClicked = {
            ifFirstGame = !ifFirstGame
            val fgColor = if (ifFirstGame) Color(0x05D817) else Color(193, 120, 12, alpha = 0.50)
            firstGame.visual = ColorVisual(fgColor).apply { style.borderRadius = BorderRadius(15) }
        }
        backButton.onMouseClicked = {
            NovaApplication.showMenuScene(NovaApplication.hostGameSceneOne)
        }
        startButton.onMouseClicked = onMouseClicked@ {
            if (playerGUIs.size < 2) {
                showError("You need at least two players!")
                return@onMouseClicked
            }

            val playersStartGame = mutableListOf<Player>()
            val tokenCount = if (ifFirstGame) when (playerGUIs.size) { 3 -> 18; 4 -> 16; else -> 21 } else 21

            for ((idx, gui) in playerGUIs.withIndex()) {
                val name = gui.nameLabel.text
                val playerType = if (name == rootService.networkService.currentSessionPlayers.first()) {
                    rootService.networkService.myPlayerType
                } else {
                    PlayerType.HUMAN
                }
                val playerColour = when (gui.colorIndex) {
                    0 -> PlayerColour.BLACK
                    1 -> PlayerColour.WHITE
                    2 -> PlayerColour.BLUE
                    3 -> PlayerColour.ORANGE
                    else -> PlayerColour.BLACK
                }
                playersStartGame.add(
                    Player(
                        playerName = name,
                        tokenCount = tokenCount,
                        moonTrackPosition = 0,
                        onlineMode = true,
                        playerType = playerType,
                        playerColour = playerColour,
                        height = 0
                    )
                )
            }
            rootService.networkService.startNewHostedGame(playersStartGame, ifFirstGame, ifRandom)
        }
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

    // Build or rebuild the player UI when player list changes
    private fun buildPlayerUIs() {
        // remove old dynamic UI
        playerGUIs.forEach { gui ->
            contentPane.remove(gui.orderToken)
            contentPane.remove(gui.nameLabel)
            contentPane.remove(gui.colorButton)
        }
        playerGUIs.clear()

        // rebuild
        rootService.networkService.currentSessionPlayers.forEachIndexed { idx, playerName ->
            val y = 296.0 + idx * 145.0
            val orderLabel = Label(
                posX = 190.0,
                posY = y,
                width = 100.0,
                height = 91.0,
                text = "${idx + 1}",
                font = Font(48, Color.BLACK, "Space Grotesk"),
                visual = ColorVisual(Color(193, 120, 12, 0.50)).apply { style.borderRadius = BorderRadius(15) }
            )

            val nameLabel = Label(
                posX = 544.0,
                posY = y,
                width = 350.0,
                height = 91.0,
                text = playerName,
                font = Font(48, Color.BLACK, "Space Grotesk"),
                visual = ColorVisual(Color(0xEEC07E)).apply { style.borderRadius = BorderRadius(15) }
            )

            val colorButton = Button(
                text = "NONE",
                width = 273.9,
                height = 91.0,
                posX = 1042.0,
                posY = y,
                font = Font(48, Color.BLACK, "Space Grotesk"),
                visual = ColorVisual(Color.GRAY).apply { style.borderRadius = BorderRadius(15) }
            ).apply {
                onMouseClicked = {
                    currentColorPickerIndex = idx
                    selectColorPane.isVisible = true
                }
            }
            playerGUIs += PlayerGUI(orderLabel, nameLabel, colorButton)
            contentPane.addAll(orderLabel, nameLabel, colorButton)
        }
    }

    // Apply color selection
    private fun applyColor(colorIdx: Int) {
        if (currentColorPickerIndex < 0) return
        val gui = playerGUIs[currentColorPickerIndex]
        if (gui.colorIndex != 4) availableColors[gui.colorIndex] = true
        gui.colorIndex = colorIdx
        gui.colorButton.apply {
            text = when (colorIdx) {0->"Black";1->"White";2->"Blue";3->"Orange";else->"NONE"}
            font = Font(48, if (colorIdx==1) Color.BLACK else Color.WHITE, "Space Grotesk")
            visual = ColorVisual(
                color = when (colorIdx) {
                0->Color.BLACK
                1->Color.WHITE
                2->Color.BLUE
                3->Color(0xFF8401)
                else->Color.GRAY }
            ).apply { style.borderRadius = BorderRadius(15) }
        }
        if (colorIdx < 4) availableColors[colorIdx] = false
        selectColorPane.isVisible = false
    }

    override fun refreshConnectionState(state: ConnectionState) {

    }
}