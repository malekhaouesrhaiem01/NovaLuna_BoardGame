package gui

import entity.Player
import entity.PlayerColour
import entity.PlayerType
import service.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.components.layoutviews.*
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.*
import tools.aqua.bgw.visual.*
import java.util.Timer
import java.util.TimerTask

/**
 * The OfflineMenu Scene/ GameConfig Scene for an Offline Game in NovaLuna
 * Implements [Refreshable] to react to service layer updates.
 *
 * @param rootService Provides access to GameService for starting new game.
 *
 * @property availableColors A List of available colors the Player can still choose from
 * @property ifRandom Boolean to determine if the Player order is Random or not
 * @property ifFirstGame Boolean to determine if it's the Players FirstGame
 * @property actualSpeed The GameSpeed of an NovaLune Bot
 */
class OfflineMenuScene (private val rootService: RootService) :
    MenuScene(1920,1080, background = ImageVisual("back_image.png")), Refreshable {

    private val availableColors = mutableListOf(true, true, true, true)
    private var ifRandom = false
    private var actualSpeed = 3
    private var ifFirstGame : Boolean = false


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


    private val botsToken = Label(
        posX = 1406.1,
        posY = 112,
        width = 273.9,
        height = 75,
        text = "Bots",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xF9B44E)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    // 1st default player
    private val firstOderToken = Label(
        posX = 190,
        posY = 296,
        width = 100,
        height = 91,
        text = "1",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val firstDefaultInput = TextField(
        prompt = "Name",
        width = 350,
        height = 91,
        posX = 544,
        posY = 296,
        font = Font(48, Color(0xFFFFFFF), "Space Grotesk"),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val firstColorButton = Button(
        text = "NONE",
        width = 273.9,
        height = 91,
        posX = 1042,
        posY = 300,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color.GRAY).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply { onMouseClicked = {
        selectColor(0)
        selectColorPane.isVisible = true
    }
    }

    private val firstEasyButton = Button(
        text = "easy",
        width = 133,
        height = 91,
        posX = 1405,
        posY = 300,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply { onMouseClicked = { setPlayerType(0,0)} }

    private val firstHardButton = Button(
        text = "hard",
        width = 133,
        height = 91,
        posX = 1547,
        posY = 300,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply { onMouseClicked = { setPlayerType(0,1)} }

    // 2nd default player
    private val secondOderToken = Label(
        posX = 190,
        posY = 296 + 54 + 91,
        width = 100,
        height = 91,
        text = "2",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val secondDefaultInput = TextField(
        prompt = "Name",
        width = 350,
        height = 91,
        posX = 544,
        posY = 296 + 54 + 91,
        font = Font(48, Color(0xFFFFFFF), "Space Grotesk"),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val secondColorButton = Button(
        text = "NONE",
        width = 273.9,
        height = 91,
        posX = 1042,
        posY = 300 + 54 + 91,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color.GRAY).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply {
        onMouseClicked = {
            selectColor(1)
            selectColorPane.isVisible = true
        }
    }

    private val secondEasyButton = Button(
        text = "easy",
        width = 133,
        height = 91,
        posX = 1405,
        posY = 300 + 54 + 91,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply { onMouseClicked = { setPlayerType(1,0)} }

    private val secondHardButton = Button(
        text = "hard",
        width = 133,
        height = 91,
        posX = 1547,
        posY = 300 + 54 + 91,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply { onMouseClicked = { setPlayerType(1,1)} }


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
     * This class stores the GUI elements associated with a player setup.
     * It is created for each player during the player configuration phase.
     *
     * @property orderToken Label that shows the order/position of the player (1 to 4).
     * @property defaultInput TextField for entering the player's name.
     * @property colorButton Button to choose the player's color.
     * @property easyButton Button to assign an easy bot to this player.
     * @property hardButton Button to assign a hard bot to this player.
     * @property removeButton Optional button to remove this player (only for players 3 and 4).
     * @property whichPlayer 0 = Human, 1 = Easy Bot, 2 = Hard Bot. Defaults to 0 (Human).
     * @property color 0 = Black, 1 = White, 2 = Blue, 3 = Orange, 4 = None. Defaults to 4.
     */
    data class PlayerGUI(
        val orderToken: Label,
        val defaultInput: TextField,
        val colorButton: Button,
        val easyButton: Button,
        val hardButton: Button,
        val removeButton: Button? = null,
        var whichPlayer : Int = 0,
        var color : Int = 4
    )


    private val player1 = PlayerGUI(firstOderToken, firstDefaultInput, firstColorButton,
        firstEasyButton, firstHardButton)

    private val player2 = PlayerGUI(secondOderToken, secondDefaultInput,
        secondColorButton, secondEasyButton, secondHardButton)

    private val players = mutableListOf(player1, player2)



    private val addButton = Button(
        text = "+",
        width = 1136,
        height = 91,
        posX = 544,
        posY = 590,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    ).apply {
        onMouseClicked = {
            addPlayer()
        }
    }

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

    private val speedButton = Label(
        text = "3s",
        width = 228,
        height = 75,
        posX = 833,
        posY = 893,
        font = Font(48, Color.BLACK, "Space Grotesk"),
        visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val speedUp = Button(
        width = 50,
        height = 50,
        posX = 833 + 12.5,
        posY = 893 + 12.5,
        visual = ImageVisual("arrow_up.png")
    ).apply {
        onMouseClicked = {
            if (actualSpeed < 10) {

                actualSpeed += 1
                speedButton.text = actualSpeed.toString() + "s"
            }
        }
    }

    private val speedDown = Button(
        width = 50,
        height = 50,
        posX = 833 + 228 - 12.5 - 50,
        posY = 893 + 12.5,
        visual = ImageVisual("arrow_down.png")
    ).apply {
        onMouseClicked = {
            if (actualSpeed > 0) {

                actualSpeed -= 1
                speedButton.text = actualSpeed.toString() + "s"
            }
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
    ).apply {
        onMouseClicked = onMouseClicked@{
            errorLabel.isVisible = false

            val playersStartGame = mutableListOf<Player>()

            val tokenCount = if (ifFirstGame) {
                when (players.size) {
                    3 -> 18
                    4 -> 16
                    else -> 21
                }
            } else {
                21
            }

            val height = 0
            val onlineMode = false
            val moonTrackPosition = 0

            for (player in players) {
                val name = player.defaultInput.text
                if (name.isEmpty()) {
                    showError("Please enter a name for all players!")
                    return@onMouseClicked
                }

                val playerType = when (player.whichPlayer) {
                    0 -> PlayerType.HUMAN
                    1 -> PlayerType.EASYBOT
                    else -> PlayerType.HARDBOT
                }

                val playerColour = when (player.color) {
                    0 -> PlayerColour.BLACK
                    1 -> PlayerColour.WHITE
                    2 -> PlayerColour.BLUE
                    3 -> PlayerColour.ORANGE
                    else -> {
                        showError("Please select a color for all players!")
                        return@onMouseClicked
                    }
                }

                playersStartGame.add(
                    Player(
                        playerName = name,
                        tokenCount = tokenCount,
                        moonTrackPosition = moonTrackPosition,
                        onlineMode = onlineMode,
                        playerType = playerType,
                        playerColour = playerColour,
                        height = height
                    )
                )
            }

            NovaApplication.apply {
                gameScene.ifOfflineMode = true
                showGameScene(gameScene)
            }

            rootService.gameService.startNewGame(
                playersStartGame,
                actualSpeed,
                ifRandom,
                ifFirstGame
            )
        }
    }

    init {
        selectColorPane.addAll(colorSelectLabel, noneColor, blackColor, whiteColor, blueColor, orangeColor)
        contentPane.addAll(orderToken, playersToken, colorsToken, botsToken,
            firstOderToken, firstDefaultInput,firstColorButton, firstEasyButton, firstHardButton,
            secondOderToken, secondDefaultInput,secondColorButton, secondEasyButton, secondHardButton,
            addButton, randomButton, backButton,speedButton, speedUp, speedDown,firstGame,startButton)
        addComponents(contentPane, selectColorPane, errorLabel)

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

    private fun selectColor(i: Int) {
        val player = players[i]

        fun setupColorButton(
            button: Button,
            colorIndex: Int,
            colorName: String,
            fontColor: Color,
            backgroundColor: Color
        ) {
            button.onMouseClicked = {
                // Free old color if assigned
                if (player.color != 4) {
                    availableColors[player.color] = true
                }
                // Set new color
                player.color = colorIndex
                player.colorButton.apply {
                    text = colorName
                    font = Font(48, fontColor, "Space Grotesk")
                    visual = ColorVisual(backgroundColor).apply {
                        style.borderRadius = BorderRadius(15)
                    }
                }
                // Mark new color as taken if not 'None'
                if (colorIndex != 4) {
                    availableColors[colorIndex] = false
                }
                selectColorPane.isVisible = false
            }
            if(colorIndex != 4){
                button.isVisible = availableColors[colorIndex]
            }
        }

        // Setup buttons with corresponding colors
        setupColorButton(noneColor, 4, "NONE", Color.BLACK, Color.GRAY)
        setupColorButton(blackColor, 0, "Black", Color.WHITE, Color.BLACK)
        setupColorButton(whiteColor, 1, "White", Color.BLACK, Color.WHITE)
        setupColorButton(blueColor, 2, "Blue", Color.BLACK, Color.BLUE)
        setupColorButton(orangeColor, 3, "Orange", Color.BLACK, Color(0xFF8401))
    }

    private fun addPlayer(){

        if (players.size == 4) return

        val i = players.size

        val posY = addButton.posY

        val newOderToken = Label(
            posX = 190,
            posY = posY,
            width = 100,
            height = 91,
            text = "${i+1}",
            font = Font(48, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                style.borderRadius = BorderRadius(15)
            }
        )

        val newDefaultInput = TextField(
            prompt = "Name",
            width = 350,
            height = 91,
            posX = 544,
            posY = posY,
            font = Font(48, Color(0xFFFFFFF), "Space Grotesk"),
            visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                style.borderRadius = BorderRadius(15)
            }
        )

        val newColorButton = Button(
            text = "NONE",
            width = 273.9,
            height = 91,
            posX = 1042,
            posY = posY,
            font = Font(48, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color.GRAY).apply {
                style.borderRadius = BorderRadius(15)
            }
        ).apply {
            onMouseClicked = {
                selectColor(i)
                selectColorPane.isVisible = true
            }
        }

        val newEasyButton = Button(
            text = "easy",
            width = 133,
            height = 91,
            posX = 1405,
            posY = posY,
            font = Font(48, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                style.borderRadius = BorderRadius(15)
            }
        ).apply {
            onMouseClicked = {
                setPlayerType(i, 0)
            }
        }
        val newHardButton = Button(
            text = "hard",
            width = 133,
            height = 91,
            posX = 1547,
            posY = posY,
            font = Font(48, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                style.borderRadius = BorderRadius(15)
            }
        ).apply {
            onMouseClicked = { setPlayerType(i, 1) }
        }

        val removeButton = Button(
            width = 100,
            height = 91,
            posX = 1732,
            posY = posY,
            visual = CompoundVisual(
                ColorVisual(Color(193, 120, 12, alpha = 0.50)).apply {
                    style.borderRadius = BorderRadius(15)},
                ImageVisual("remove.png")
            )
        ).apply {
            onMouseClicked = { removePlayer(i)}
        }

        contentPane.addAll(newOderToken, newDefaultInput,
            newColorButton, newEasyButton, newHardButton, removeButton)

        val newPlayer = PlayerGUI(
            newOderToken,
            newDefaultInput,
            newColorButton,
            newEasyButton,
            newHardButton,
            removeButton
        )

        players.add(newPlayer)

        addButton.posY = posY + addButton.height + 58

        if (i+1 == 4){
            addButton.isVisible = false
        }
    }

    private fun removePlayer(i: Int){


        val player = players[i]
        if(player.color != 4){
            availableColors[player.color] = true
        }
        contentPane.remove(player.orderToken)
        contentPane.remove(player.defaultInput)
        contentPane.remove(player.colorButton)
        contentPane.remove(player.easyButton)
        contentPane.remove(player.hardButton)
        if (player.removeButton != null) {
            contentPane.remove(player.removeButton)
        }
        players.removeAt(i)

        val lastPlayer = players.last()

        if (i+1 == 3 && players.size > 2){
            val offset = 58 + 91
            lastPlayer.orderToken.text = "3"
            lastPlayer.orderToken.posY -= offset
            lastPlayer.defaultInput.posY -= offset
            lastPlayer.colorButton.posY -= offset
            lastPlayer.easyButton.posY -= offset
            lastPlayer.hardButton.posY -= offset
            if (lastPlayer.removeButton != null){
                lastPlayer.removeButton.posY -= offset
                lastPlayer.removeButton.onMouseClicked = {removePlayer(i)}
                lastPlayer.easyButton.onMouseClicked = {setPlayerType(i, 0)}
                lastPlayer.hardButton.onMouseClicked = {setPlayerType(i, 1)}
                lastPlayer.colorButton.onMouseClicked = {
                    selectColor(i)
                    selectColorPane.isVisible = true
                }
            }
        }

        addButton.posY = players.last().defaultInput.posY + 91 + 58
        addButton.isVisible = true

    }

    private fun setPlayerType(i: Int, j: Int){

        val player = players[i]
        val visualOff = ColorVisual(Color(193, 120, 12)).apply {
            style.borderRadius = BorderRadius(15)
            transparency = 0.5
        }
        val visualOn = ColorVisual(Color.GREEN).apply {
            style.borderRadius = BorderRadius(15)
            transparency = 0.5
        }
        if (j == 0){
            when (player.whichPlayer) {
                0 -> {
                    player.easyButton.visual = visualOn
                    player.whichPlayer = 1

                }
                1 -> {
                    player.easyButton.visual = visualOff
                    player.whichPlayer = 0
                }
                else -> {
                    player.hardButton.visual = visualOff
                    player.easyButton.visual = visualOn
                    player.whichPlayer = 1
                }
            }
        }else{
            when (player.whichPlayer) {
                0 -> {
                    player.hardButton.visual = visualOn
                    player.whichPlayer = 2

                }
                1 -> {
                    player.easyButton.visual = visualOff
                    player.hardButton.visual = visualOn
                    player.whichPlayer = 2
                }
                else -> {
                    player.hardButton.visual = visualOff
                    player.whichPlayer = 0
                }
            }
        }

    }

    /**
     * Resets the offline menu scene to its initial state.
     * Clears all player names, resets colors, and other settings.
     */
    fun resetMenuState() {
        // Reset text fields
        firstDefaultInput.text = ""
        secondDefaultInput.text = ""
        
        // Reset color availability and buttons
        availableColors.fill(true)
        firstColorButton.text = "NONE"
        secondColorButton.text = "NONE"
        
        // Reset player types and colors for initial players
        val visualOff = ColorVisual(Color(193, 120, 12)).apply {
            style.borderRadius = BorderRadius(15)
            transparency = 0.5
        }
        
        // Reset first player
        player1.whichPlayer = 0 // Human
        player1.color = 4 // No color selected
        player1.easyButton.visual = visualOff
        player1.hardButton.visual = visualOff
        
        // Reset second player
        player2.whichPlayer = 0 // Human
        player2.color = 4 // No color selected
        player2.easyButton.visual = visualOff
        player2.hardButton.visual = visualOff
        
        // Reset any dynamically added players to initial state (only 2 players)
        while (players.size > 2) {
            val removedPlayer = players.removeAt(players.lastIndex)
            // Remove the UI components from the scene
            contentPane.remove(removedPlayer.orderToken)
            contentPane.remove(removedPlayer.defaultInput)
            contentPane.remove(removedPlayer.colorButton)
            contentPane.remove(removedPlayer.easyButton)
            contentPane.remove(removedPlayer.hardButton)
            removedPlayer.removeButton?.let { contentPane.remove(it) }
        }
        
        // Reset other settings
        ifRandom = false
        actualSpeed = 3
        ifFirstGame = false
        
        println("DEBUG: OfflineMenuScene state reset - players: ${players.size}, colors available: $availableColors")
    }
}