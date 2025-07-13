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

        selectColorPane.addAll(colorSelectLabel, noneColor, blackColor, whiteColor, blueColor, orangeColor)
        contentPane.addAll(orderToken, playersToken, colorsToken, randomButton, backButton, firstGame, startButton, errorLabel)
        addComponents(contentPane, selectColorPane)

        // bind colorPane buttons once
        noneColor.onMouseClicked   = { selectColor(currentColorPickerIndex) }
        blackColor.onMouseClicked  = { selectColor(currentColorPickerIndex) }
        whiteColor.onMouseClicked  = { selectColor(currentColorPickerIndex) }
        blueColor.onMouseClicked   = { selectColor(currentColorPickerIndex) }
        orangeColor.onMouseClicked = { selectColor(currentColorPickerIndex) }

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
            NovaApplication.apply {
                onlineGameScene.ifOnlineMode = true
                showGameScene(onlineGameScene)
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
                onMouseClicked = { selectColor(idx) }
            }
            playerGUIs += PlayerGUI(orderLabel, nameLabel, colorButton)
            contentPane.addAll(orderLabel, nameLabel, colorButton)
        }
    }

    // Apply color selection
    private fun selectColor(i: Int) {
        if (i < 0 || i >= playerGUIs.size) return
        currentColorPickerIndex = i
        val gui = playerGUIs[i]

        fun setup(
            button: Button,
            idx: Int,
            name: String,
            fontColor: Color,
            bg: Color
        ) {
            button.isVisible = if (idx == 4) true else availableColors[idx]

            button.onMouseClicked = {

                if (gui.colorIndex in 0..3) {
                    availableColors[gui.colorIndex] = true
                }

                gui.colorIndex = idx
                gui.colorButton.apply {
                    text = name
                    font = Font(48, fontColor, "Space Grotesk")
                    visual = ColorVisual(bg).apply { style.borderRadius = BorderRadius(15) }
                }

                if (idx in 0..3) {
                    availableColors[idx] = false
                }
                selectColorPane.isVisible = false
            }
        }

        setup(noneColor,   4, "NONE",  Color.WHITE, Color.GRAY)
        setup(blackColor,  0, "Black", Color.WHITE, Color.BLACK)
        setup(whiteColor,  1, "White", Color.BLACK, Color.WHITE)
        setup(blueColor,   2, "Blue",  Color.WHITE, Color.BLUE)
        setup(orangeColor, 3, "Orange",Color.WHITE, Color(0xFF8401))

        selectColorPane.isVisible = true
    }

    /**
     * Refreshes the scene after a player has joined.
     * This method is called by the [RootService] to update the player list.
     */
    override fun refreshAfterPlayerJoined() {
        // always rebuild the list of player rows
        // and do it on the BGW-UI thread


        buildPlayerUIs()

    }
    //override fun refreshConnectionState(state: ConnectionState) {}
}