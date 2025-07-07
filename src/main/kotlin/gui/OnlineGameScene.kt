package gui

import entity.*
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameApplication.Companion.runOnGUIThread
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Coordinate
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.util.*


/**
 *  The Online GameScene for an online Game in NovaLuna.
 *
 *  Implements [Refreshable] to react to service layer updates.
 *
 *  @constructor Creates a new online GameScene with the specified rootService.
 *
 *  @param rootService The [RootService] that manages the game state.
 *  @property tileCoordinates A List of Tile Coordinates
 *  @property tokenCoordinates A List of Token Coordinates
 *  @property tokensOnTheMoonWheel A List to display the tokens on the MoonWheel
 *  @property tilesOnTheMoonWheel A List to store the Tiles on the MoonWheel
 *  @property chosenTile The Tile chosen by the Player
 *  @property playerComponents A List to Display the different Players tiles
 *  @property isAlreadyPlayed Boolean to determine if a Player has Played a Tile
 *
 */
class OnlineGameScene(private val rootService: RootService): BoardGameScene(1920, 1080), Refreshable {

    val tileCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()
    val tokenCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()

    val tilesOnTheMoonWheel: MutableList<TileGUI> = mutableListOf()
    val tokensOnTheMoonWheel: MutableList<Label> = mutableListOf()
    var playerComponents: MutableList<ComponentView> = mutableListOf()

    var chosenTile: Pair<ComponentView, Int>? = null
    var isAlreadyPlayed: Boolean = false

    data class TileGUI(

        val label: ComponentView,
        val id: Int
    )

    private val contentPane = Pane<ComponentView>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ImageVisual("back_image.png")
    )

    val playersHand = Pane<ComponentView>(
        width = 1920,
        height = 1080,
        visual = ImageVisual("back_image.png")
    ).apply {
        isVisible = false
    }

    val moonWheel = Label(
        posX = 610,
        posY = 125,
        width = 700,
        height = 700,
        visual = ImageVisual(path = "moonWheel.png")
    )

    val endTurnButton = Button(
        posX = 1515,
        posY = 953,
        width = 320,
        height = 92,
        text = "end turn",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
    ).apply {
        onMouseClicked = {
            if (!isAlreadyPlayed) {
                showError("First, you have to make a move")
            }else{
                rootService.gameService.endTurn()
            }
        }
    }


    // Draw Stack
    val drawStackButton = Button(
        posX = 1515,
        posY = 35,
        width = 307,
        height = 60,
        text = "Draw Pile Tiles",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xFFCC81)).apply {
            style.borderRadius = BorderRadius(10); transparency = 0.84
        }
    ).apply { onMouseClicked = {overlayPaneDrawStack.isVisible = true} }



    val gridPaneDrawStack = GridPane<ComponentView>(
        posX = 960,
        posY = 540,
        rows = 4,
        columns = 17,
        spacing = 5,
        layoutFromCenter = true
    )

    val overlayPaneDrawStack = Pane<ComponentView>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ColorVisual(Color(0xF8E8CB))
    ).apply {
        isVisible = false
        add(gridPaneDrawStack)

        val backButton = Button(
            posX = 839.5,
            posY = 780,
            width = 241,
            height = 75,
            text = "back",
            alignment = Alignment.CENTER,
            font = Font(48, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
        ).apply {
            onMouseClicked = { hideDrawStack() }
        }

        add(backButton)
    }

    val drawPile = Button(
        posX = 955 - 75,
        posY = 475 - 75,
        width = 150,
        height = 150,
        font = Font(48, Color(0xFFBA33),"Space Grotesk" ),
        visual = ImageVisual("tileBack.png")
    ).apply {
        onMouseClicked = {
            if(!isAlreadyPlayed){
                rootService.playerActionService.refillWheel()
            }
        }
    }

    val playerNameForNextPlayerPane = Label(
        posX = 710,
        posY = 448,
        width = 500,
        height = 120,
        text = "",
        alignment = Alignment.CENTER,
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xD39130)).apply { style.borderRadius = BorderRadius(10) }
    )

    val nextPlayerPane = Pane<ComponentView>(
        width = 1920,
        height = 1080,
        posX = 0,
        posY = 0,
        visual = ColorVisual(Color(0x8B570C)).apply { transparency = 0.5; style.borderRadius = BorderRadius(10)  }
    ).apply {

        isVisible = false

        val back = Label(
            width = 800,
            height = 500,
            posX = 560,
            posY = 290,
            visual = ColorVisual(Color(0xFFCC81)).apply {
                style.borderRadius = BorderRadius(10);style.borderRadius = BorderRadius(10)}
        )

        val text = Label(
            posX = 820,
            posY = 333,
            width = 281,
            height = 61,
            text = "Next Player:",
            font = Font(48, Color.BLACK,"Space Grotesk" )
        )

        addAll(back, text, playerNameForNextPlayerPane)

    }

    private val errorLabel = Label(
        text = "",
        width = 700,
        height = 100,
        posX = width / 2 - 350,
        posY = height / 2 - 50,
        font = Font(30, Color.WHITE),
        visual = ColorVisual(Color(200,50,50,230))
    ).apply {
        isVisible = false
    }


    init {

        setCoordinatesForTheMoonWheel()
        setTokenCoordinatesForTheMoonWheel()

        contentPane.addAll(moonWheel, endTurnButton, drawStackButton, drawPile)

        addComponents(contentPane, playersHand, overlayPaneDrawStack, nextPlayerPane, errorLabel)

    }

    override fun refreshAfterStartGame() {

        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        updateDrawStack(game)
        rootService.gameService.startTurn()

    }

    override fun refreshAfterStartTurn(){
        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")

        checkIfHumanAndLocalPlayer(game)

        updateMoonWheel(game)
        setTokens(game)

        createLocalPlayerHand(game)
        createPlayersHands(game)

        if(game.players[game.activePlayer].playerType == PlayerType.EASYBOT){
            rootService.easyBotService.executeEasyMove()
        }

    }

    override fun refreshAfterEndTurn() {
        chosenTile = null
        isAlreadyPlayed = false
        clearPlayersDisplay()

        val game = rootService.currentGame
        checkNotNull(game){"Error in refreshAfterEndTurn"}
        playerNameForNextPlayerPane.text = game.players[game.activePlayer].playerName
        nextPlayerPane.isVisible = true

        playAnimation(
            DelayAnimation(duration = 2000).apply {
                onFinished = {
                    nextPlayerPane.isVisible = false
                    rootService.gameService.startTurn()
                }
            },
        )

    }

    override fun refreshAfterTilePlayed(){
        clearPlayersDisplay()

        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        updateMoonWheel(game)
        setTokens(game)
        createLocalPlayerHand(game)
        createPlayersHands(game)

        playersHand.isVisible = false

    }


    override fun refreshAfterRefill() {
        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")

        updateMoonWheel(game)
        updateDrawStack(game)
    }

    override fun refreshAfterGameEnd(winner: Player) {
        chosenTile = null
        isAlreadyPlayed = false
        clearPlayersDisplay()
    }

    private fun createLocalPlayerHand(game: NovaLunaGame){

        //  get the local player
        val localPlayer = game.players.firstOrNull { !it.onlineMode }
        checkNotNull(localPlayer){"Error in createLocalPlayerHand"}

        val colorVisual = if(localPlayer == game.players[game.activePlayer]){
            ColorVisual(Color.GREEN).apply { transparency = 0.7 }
        }else{
            ColorVisual(Color(0xC1780C))
        }

        val localPlayerButton = Button(
            posX = 673,
            posY = 953,
            width = 606,
            height = 96,
            text = "${localPlayer.playerName} : ${localPlayer.tokenCount} tokens left",
            alignment = Alignment.CENTER,
            font = Font(36, getPlayerColor(localPlayer.playerColour), "Space Grotesk"),
            visual = colorVisual.apply { style.borderRadius = BorderRadius(10) }
        ).apply {
            onMouseClicked = {
                showLocalPlayerHand(localPlayer)
                playersHand.isVisible = true
            }
        }

        playerComponents.add(localPlayerButton)
        contentPane.add(localPlayerButton)
    }

    private fun showLocalPlayerHand(player: Player){

        playersHand.clear()

        val playersName = Label(
            posX = 657,
            posY = 35,
            width = 606,
            height = 96,
            text = "${player.playerName} : ${player.tokenCount} tokens left",
            alignment = Alignment.CENTER,
            font = Font(36, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xFFCC81)).apply { style.borderRadius = BorderRadius(10) }
        )
        val back =  Button(
            posX = 61,
            posY = 955,
            width = 241,
            height = 75,
            text = "back",
            alignment = Alignment.CENTER,
            font = Font(48, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
        ).apply {
            onMouseClicked = {
                playersHand.isVisible = false
            }
        }

        playersHand.addAll(playersName, back)

        showWithPossiblePositions(player)

    }

    private fun showWithPossiblePositions(player: Player){

        val game = rootService.currentGame
        checkNotNull(game){"Error in showWithPossiblePositions"}

        val ifActivePlayer = game.players[game.activePlayer] == player
        val ifHuman = player.playerType == PlayerType.HUMAN

        val validPositions = rootService.gameService.getPossiblePosition()

        val minX = validPositions.minOfOrNull { it.xCoord.toInt() } ?: 0
        val maxX = validPositions.maxOfOrNull { it.xCoord.toInt() } ?: 0
        val minY = validPositions.minOfOrNull { it.yCoord.toInt() } ?: 0
        val maxY = validPositions.maxOfOrNull { it.yCoord.toInt() } ?: 0

        val column = maxX - minX + 1
        val row = maxY - minY + 1
        val offsetX = -minX
        val offsetY = maxY


        val gridHand = GridPane<ComponentView>(
            posX = 929,
            posY = 382.5,
            rows = row,
            columns = column,
            spacing = 5
        ).apply {
            placeTiles(this, player, offsetX, offsetY)
            if (!isAlreadyPlayed && ifHuman && ifActivePlayer) {
                placePossiblePositions(this, validPositions, offsetX, offsetY)
            }

        }


        val cameraHand = CameraPane(
            posX = 37,
            posY = 161,
            width = 1858,
            height = 765,
            target = gridHand
        ).apply {
            interactive = true
        }

        playersHand.add(cameraHand)

    }


    private fun createPlayersHands(game: NovaLunaGame){


        val grid = GridPane<ComponentView>(
            posX = 59,
            posY = 86,
            layoutFromCenter = false,
            rows = game.players.size,
            columns = 1,
            spacing = 20
        )

        for ((i,player) in game.players.withIndex()) {

            if (!player.onlineMode){continue}

            val colorVisual = if(player == game.players[game.activePlayer]){
                ColorVisual(Color.GREEN).apply { transparency = 0.7 }
            }else{
                ColorVisual(Color(0xC1780C))
            }
            val playerLabel = Button(
                width = 292,
                height = 199,
                text = "${player.playerName} : ${player.tokenCount} tokens left",
                alignment = Alignment.CENTER,
                font = Font(36, getPlayerColor(player.playerColour), "Space Grotesk"),
                visual = colorVisual.apply { style.borderRadius = BorderRadius(10) }
            ).apply {
                onMouseClicked = {
                    showPlayerHand(player)
                    playersHand.isVisible = true
                }
            }

            grid[0,i] = playerLabel

        }

        playerComponents.add(grid)
        contentPane.addAll(grid)

    }

    private fun showPlayerHand(player: Player){

        playersHand.clear()

        val playersName = Label(
            posX = 657,
            posY = 35,
            width = 606,
            height = 96,
            text = "${player.playerName} : ${player.tokenCount} tokens left",
            alignment = Alignment.CENTER,
            font = Font(36, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xFFCC81)).apply { style.borderRadius = BorderRadius(10) }
        )

        val back =  Button(
            posX = 61,
            posY = 955,
            width = 241,
            height = 75,
            text = "back",
            alignment = Alignment.CENTER,
            font = Font(48, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
        ).apply {
            onMouseClicked = {
                playersHand.isVisible = false
            }
        }

        playersHand.addAll(playersName, back)

        showWithoutPossiblePositions(player)

    }

    private fun showWithoutPossiblePositions(player: Player){

        val minX = player.tiles.minOfOrNull { it?.position!!.xCoord.toInt() } ?: 0
        val maxX = player.tiles.maxOfOrNull { it?.position!!.xCoord.toInt() } ?: 0
        val minY = player.tiles.minOfOrNull { it?.position!!.yCoord.toInt() } ?: 0
        val maxY = player.tiles.maxOfOrNull { it?.position!!.yCoord.toInt() } ?: 0

        val column = maxX - minX + 1
        val row = maxY - minY + 1

        val offsetX = -minX
        val offsetY = maxY

        val gridHand = GridPane<ComponentView>(
            posX = 929,
            posY = 382.5,
            rows = row,
            columns = column,
            spacing = 5
        ).apply {
            placeTiles(this, player, offsetX, offsetY)
        }


        val cameraHand = CameraPane(
            posX = 37,
            posY = 161,
            width = 1858,
            height = 765,
            target = gridHand
        ).apply {
            interactive = true
        }

        playersHand.add(cameraHand)

    }

    private fun updateMoonWheel(game: NovaLunaGame){

        // clear the MoonWheel
        for (tile in tilesOnTheMoonWheel){
            contentPane.remove(tile.label)
        }

        tilesOnTheMoonWheel.clear()


        // Full the MoonWheel
        val tiles = game.tileTrack
        val ifLocalPlayer = !game.players[game.activePlayer].onlineMode
        val ifHuman = game.players[game.activePlayer].playerType == PlayerType.HUMAN


        val availableTiles: List<Int?> = rootService.gameService.getAvailableTiles()

        for((i, tile) in tiles.withIndex()){
            if (tile != null) {
                val tileLabel = createTile(tile).apply {
                    posX = tileCoordinates[i].first.toDouble()
                    posY = tileCoordinates[i].second.toDouble()
                }

                val toAdd: ComponentView = if
                        (i in availableTiles.filterNotNull() && !isAlreadyPlayed && ifHuman && ifLocalPlayer) {
                    tileLabel.apply { posX = 5.0; posY = 5.0
                    }

                    Pane<ComponentView>(
                        posX = tileCoordinates[i].first.toDouble() - 5,
                        posY = tileCoordinates[i].second.toDouble() - 5,
                        height = 110,
                        width = 110,
                        visual = ColorVisual(Color.GREEN).apply { style.borderRadius = BorderRadius(10) }
                    ).apply {
                        onMouseClicked = {
                            setTile(this, i)
                        }
                        addAll(tileLabel)
                    }
                } else {
                    tileLabel
                }

                tilesOnTheMoonWheel.add(TileGUI(toAdd, tile.id))
                contentPane.add(toAdd)
            }
        }

        val meeple = Label(
            posX = tileCoordinates[game.meeplePosition].first,
            posY = tileCoordinates[game.meeplePosition].second,
            width = 100,
            height = 100,
            visual = ImageVisual("meeple.png")
        )

        tilesOnTheMoonWheel.add(TileGUI(meeple, 100))
        contentPane.add(meeple)
    }

    private fun getColor(tileColor : TileColour): Color{
        val color = when(tileColor){
            TileColour.RED -> {
                Color.RED
            }
            TileColour.BLUE -> {
                Color.BLUE
            }
            TileColour.YELLOW -> {
                Color(0xFFBA33)
            }
            else -> {
                Color(0x29E4E4)
            }
        }
        return color
    }

    private fun getPlayerColor(playerColor : PlayerColour): Color{
        val color = when(playerColor){
            PlayerColour.WHITE -> {
                Color.WHITE
            }
            PlayerColour.ORANGE -> {
                Color.ORANGE
            }
            PlayerColour.BLUE -> {
                Color.BLUE
            }
            else -> {
                Color.BLACK
            }
        }
        return color
    }

    private fun createTile(tile: Tile): Pane<ComponentView>{
        val color = getColor(tile.tileColour)

        val tileGridPane = GridPane<ComponentView>(
            layoutFromCenter = false,
            rows = 2,
            columns = 2,
            spacing = 0, //5
            visual = ColorVisual(color).apply { style.borderRadius = BorderRadius(10)}
        )


        val circleTL = Label(
            width = 50,//61.5,
            height = 50, //61.5,
            text = "${tile.time}",
            alignment = Alignment.CENTER,
            font = Font(28, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color.WHITE).apply {
                style.borderRadius = BorderRadius(100)
            }
        )

        tileGridPane[0,0] = circleTL

        for ((index, task) in tile.tasks.withIndex()) {
            val taskPane = createTask(task.first)

            val xy = when(index) {
                0 -> Pair(1, 0)
                1 -> Pair(0, 1)
                else -> Pair(1, 1) // optional: fallback case
            }

            tileGridPane[xy.first, xy.second] = taskPane

        }

        val tilePane = Pane<ComponentView>(
            width = 100,
            height = 100
        )
        tilePane.add(tileGridPane)

        return tilePane
    }

    private fun createTask(task : Map<TileColour, Int>): Pane<ComponentView>{

        val mainPane =  Pane<ComponentView>(
            width = 50, //61.5,
            height = 50,//61.5,
            visual = ColorVisual(Color.WHITE).apply { style.borderRadius = BorderRadius(100) }
        )

        val taskPane = GridPane<ComponentView>(
            posX = 5,//9,
            posY = 5,//9,
            layoutFromCenter = false,
            rows = 2,
            columns = 2,
            spacing = 0, //2,
            visual = ColorVisual(Color.WHITE).apply { style.borderRadius = BorderRadius(100) }
        )

        val colorPositions = mutableListOf(
            Pair(0,0),
            Pair(1,0),
            Pair(0,1),
            Pair(1,1))
        var currentColor = 0

        for ((key, value) in task) {
            val color = getColor(key)
            val label = Label(
                width = 20,
                height = 20,
                visual = ColorVisual(color).apply { style.borderRadius = BorderRadius(100) }
            )

            for (i in 0 until value ){
                val xy = colorPositions[currentColor]
                taskPane[xy.first,xy.second] = label
                currentColor+=1
            }

        }

        mainPane.add(taskPane)

        return mainPane

    }

    private fun setTile(tileLabel: ComponentView, idx: Int ){
        if (chosenTile != null){
            if (chosenTile!!.first == tileLabel){
                chosenTile = null
                tileLabel.scale = 1.0
            }
            else{
                chosenTile!!.first.scale = 1.0
                chosenTile = tileLabel to idx
                tileLabel.scale = 1.5
            }
        }
        else{
            chosenTile = tileLabel to idx
            tileLabel.scale = 1.5
        }
    }

    private fun updateDrawStack(game: NovaLunaGame){
        // clear the grid
        for(row in 0 until gridPaneDrawStack.rows){
            for(column in 0 until gridPaneDrawStack.columns){
                gridPaneDrawStack[column,row] = null
            }
        }

        for( (index, tile) in game.drawPile.withIndex()){

            val column = index % 17
            val row = index / 17

            gridPaneDrawStack[column,row] = tile?.let { createTile(it) }


        }

        drawPile.text = game.drawPile.size.toString()

    }

    private fun placePossiblePositions(grid: GridPane<ComponentView>,
                                       positions: List<Coordinate>,
                                       offsetX: Int, offsetY: Int){

        for ( coord in positions){

            val label = Label(
                width = 100,
                height = 100,
                visual = ColorVisual(Color.GREEN).apply { transparency = 0.7 }
            ).apply {
                onMouseClicked = {
                    if (chosenTile == null){
                        showError("First, you have to select a tile")
                    }
                    else{
                        isAlreadyPlayed = true
                        rootService.playerActionService.playTile(chosenTile!!.second, coord)
                    }
                }
            }


            val x = coord.xCoord.toInt()
            val y = coord.yCoord.toInt()

            grid[x + offsetX, -y + offsetY] = label
        }
    }

    private fun placeTiles(grid: GridPane<ComponentView>, player: Player, offsetX: Int, offsetY: Int){

        for (tile in player.tiles){

            val tileLabel = createTile(tile!!)

            for ((index, task) in tile.tasks.withIndex()){
                if (task.second){

                    val xy = when(index){
                        0 -> 50 to 0
                        1 -> 0 to 50
                        else -> 50 to 50
                    }

                    val complited = Label(
                        posX = xy.first,
                        posY = xy.second,
                        width = 50,
                        height = 50,
                        visual = ColorVisual(getPlayerColor(player.playerColour)).apply {
                            style.borderRadius = BorderRadius(100)
                        }
                    )

                    tileLabel.add(complited)

                }
            }

            val x = tile.position!!.xCoord.toInt()
            val y = tile.position!!.yCoord.toInt()

            grid[x + offsetX, -y + offsetY] = tileLabel
        }
    }

    private fun hideDrawStack(){
        overlayPaneDrawStack.isVisible = false
    }


    private fun checkIfHumanAndLocalPlayer(game: NovaLunaGame){
        val ifLocalPlayer = !game.players[game.activePlayer].onlineMode
        val ifHuman = game.players[game.activePlayer].playerType == PlayerType.HUMAN

        endTurnButton.isVisible = ifLocalPlayer && ifHuman
        drawPile.isDisabled = !(ifLocalPlayer && ifHuman)
    }

    /**
     * Shows the given error message in [errorLabel] and hides it after 4 seconds.
     *
     * @param message The error message to display.
     */
    private fun showError(message: String) {
        errorLabel.text = message
        errorLabel.isVisible = true

        // Versteckt das Label nach 4.000 ms
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnGUIThread {
                    errorLabel.isVisible = false
                }
            }
        }, 4000)
    }

    fun setCoordinatesForTheMoonWheel(){
        // 1510, 110
        tileCoordinates.add(Pair(910, 25))
        tileCoordinates.add(Pair(1095, 80))
        tileCoordinates.add(Pair(1245, 230))
        tileCoordinates.add(Pair(1290, 420))
        tileCoordinates.add(Pair(1257, 590))
        tileCoordinates.add(Pair(1100, 760))
        tileCoordinates.add(Pair(910, 800))
        tileCoordinates.add(Pair(725, 760))
        tileCoordinates.add(Pair(542, 590))
        tileCoordinates.add(Pair(525, 420))
        tileCoordinates.add(Pair(555, 230))
        tileCoordinates.add(Pair(695, 80))
    }

    private fun setTokenCoordinatesForTheMoonWheel(){

        tokenCoordinates.add(937 to 233)
        tokenCoordinates.add(995 to 242)
        tokenCoordinates.add(1048 to 265)
        tokenCoordinates.add(1093 to 301)
        tokenCoordinates.add(1128 to 347)
        tokenCoordinates.add(1149 to 401)
        tokenCoordinates.add(1156 to 458)
        tokenCoordinates.add(1148 to 514)
        tokenCoordinates.add(1126 to 567)
        tokenCoordinates.add(1091 to 612)
        tokenCoordinates.add(1046 to 646)
        tokenCoordinates.add(994 to 669)
        tokenCoordinates.add(936 to 677)
        tokenCoordinates.add(879 to 669)
        tokenCoordinates.add(826 to 648)
        tokenCoordinates.add(780 to 613)
        tokenCoordinates.add(744 to 567)
        tokenCoordinates.add(722 to 513)
        tokenCoordinates.add(715 to 456)
        tokenCoordinates.add(721 to 399)
        tokenCoordinates.add(744 to 345)
        tokenCoordinates.add(780 to 298)
        tokenCoordinates.add(826 to 263)
        tokenCoordinates.add(880 to 241)
    }

    fun setTokens(game: NovaLunaGame){

        for (label in tokensOnTheMoonWheel){
            contentPane.remove(label)
        }
        tokensOnTheMoonWheel.clear()

        for (i in 0 until 4) {
            for(player in game.players){
                if (player.height - 1 == i){
                    val pos = player.moonTrackPosition % 24

                    val token = Label(
                        posX = tokenCoordinates[pos].first,
                        posY = tokenCoordinates[pos].second,
                        height = 35,
                        width = 35,
                        visual = ColorVisual(getPlayerColor(player.playerColour)).apply { style.borderRadius = BorderRadius(100) }
                    )

                    contentPane.add(token)
                    tokensOnTheMoonWheel.add(token)
                }
            }
        }

    }



    private fun clearPlayersDisplay(){
        for(player in playerComponents){
            contentPane.remove(player)
        }

        playerComponents.clear()
    }


}