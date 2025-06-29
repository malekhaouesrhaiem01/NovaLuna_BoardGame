package gui

import entity.NovaLunaGame
import entity.Player
import entity.Tile
import entity.TileColour
import org.w3c.dom.Text
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.LayoutView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.style.Cursor
import tools.aqua.bgw.util.Coordinate
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import kotlin.random.Random

class GameScene(private val rootService: RootService): BoardGameScene(1920, 1080), Refreshable {

    val tileCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()
    val tilesOnTheMoonWheel: MutableList<TileGUI> = mutableListOf()
    var chosenTile: Pair<ComponentView, Int>? = null
    var playerComponents: MutableList<ComponentView> = mutableListOf()
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

    val moonWheel = Label(
        posX = 610,
        posY = 125,
        width = 700,
        height = 700,
        visual = ImageVisual(path = "MoonWheel.png")
    )

    val undoButton = Button(
        posX = 78,
        posY = 964,
        width = 106,
        height = 49,
        text = "undo",
        font = Font(36, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
    )

    val redoButton = Button(
        posX = 224,
        posY = 964,
        width = 106,
        height = 49,
        text = "redo",
        font = Font(36, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
    )

    val endTurnButton = Button(
        posX = 1515,
        posY = 833,
        width = 320,
        height = 92,
        text = "end turn",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
    ).apply {
        onMouseClicked = {
            if (!isAlreadyPlayed) throw IllegalStateException("First, you have to make a move")
            rootService.gameService.endTurn()
        }
    }

    val rageQuitButton = Button(
        posX = 1515,
        posY = 953,
        width = 320,
        height = 92,
        text = "rage quit",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0x631313)).apply { style.borderRadius = BorderRadius(10); transparency = 0.84 }
    ).apply {
    }

    val drawStackLabel = Label(
        posX = 1515,
        posY = 35,
        width = 307,
        height = 60,
        text = "Draw Pile Tiles",
        font = Font(48, Color.BLACK,"Space Grotesk" ),
        visual = ColorVisual(Color(0xFFCC81)).apply { style.borderRadius = BorderRadius(10); transparency = 0.84 }
    )

    val gridPaneStack = GridPane<ComponentView>(
        rows = 23,
        columns = 3,
        spacing = 5,
        layoutFromCenter = false
    )

    val cameraPaneStack = CameraPane(
        posX = 1510,
        posY = 110,
        width = 350,
        height = 700,
        target = gridPaneStack
    ).apply {
        interactive = true

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
            rootService.playerActionService.refillWheel()
        }
    }

    val playersHand = Pane<ComponentView>(
        width = 1920,
        height = 1080,
        visual = ImageVisual("back_image.png")
    ).apply {
        isVisible = false
    }



    init {

        setCoordinatesForTheMoonWheel()

        contentPane.addAll(moonWheel, undoButton, redoButton, endTurnButton,rageQuitButton,drawStackLabel, drawPile, cameraPaneStack)

        addComponents(contentPane, playersHand)

    }

    override fun refreshAfterStartGame() {

        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        fullMoonWheel(game)
        addCurrentPlayer(game)
        addPlayers(game)
        //updateDrawStack(game)
        drawPile.text = game.drawPile.size.toString()

    }

    override fun refreshAfterStartTurn(){
        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        fullMoonWheel(game)
        addCurrentPlayer(game)
        addPlayers(game)
    }

    override fun refreshAfterEndTurn() {
        chosenTile = null
        isAlreadyPlayed = false
        clearMoonWheel()
        clearPlayersDisplay()
    }

    override fun refreshAfterTilePlayed(){
        clearMoonWheel()
        clearPlayersDisplay()

        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        fullMoonWheel(game)
        addCurrentPlayer(game)
        addPlayers(game)

        playersHand.isVisible = false

    }

    override fun refreshAfterRefill() {
        val game = rootService.currentGame ?: throw IllegalStateException("No game is currently running")
        clearMoonWheel()
        fullMoonWheel(game)
        //updateDrawStack(game)
        drawPile.text = game.drawPile.size.toString()
    }

    fun clearPlayersDisplay(){
        for(player in playerComponents){
            contentPane.remove(player)
        }

        playerComponents.clear()
    }

    fun clearMoonWheel(){

        for (tile in tilesOnTheMoonWheel){
            contentPane.remove(tile.label)
        }

        tilesOnTheMoonWheel.clear()

    }

    fun fullMoonWheel(game: NovaLunaGame){

        val tiles = game.tileTrack

        val availableTiles: List<Int?> = rootService.gameService.getAvailableTiles()

        for((i, tile) in tiles.withIndex()){
            if (tile != null) {
                val tileLabel = createTile(tile).apply {
                    posX = tileCoordinates[i].first.toDouble()
                    posY = tileCoordinates[i].second.toDouble()
                }

                val toAdd: ComponentView = if (i in availableTiles.filterNotNull() && !isAlreadyPlayed) {
                    tileLabel.apply {
                        posX = 5.0
                        posY = 5.0
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

    fun getColor(tileColor : TileColour): Color{
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

    fun createTile(tile: Tile): Pane<ComponentView>{
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

    fun createTask(task : Map<TileColour, Int>): Pane<ComponentView>{

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

        val colorPositions = mutableListOf(Pair(0,0), Pair(1,0), Pair(0,1), Pair(1,1))
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

    fun setTile(tileLabel: ComponentView, idx: Int ){
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

    fun updateDrawStack(game: NovaLunaGame){
        // clear the grid
        for(row in 0 until gridPaneStack.rows){
            for(column in 0 until gridPaneStack.columns){
                gridPaneStack[column,row] = null
            }
        }

        for( (index, tile) in game.drawPile.withIndex()){

            val column = index % 3
            val row = index / 3

            gridPaneStack[column,row] = tile?.let { createTile(it) }


        }
    }

    fun addCurrentPlayer(game: NovaLunaGame){

        val player = game.players[game.activePlayer]


        val activePlayer = Button(
            posX = 673,
            posY = 953,
            width = 606,
            height = 96,
            text = "${player.playerName} : ${player.tokenCount} tokens left",
            alignment = Alignment.CENTER,
            font = Font(36, Color(0x000000), "Space Grotesk"),
            visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
        ).apply {
            onMouseClicked = {
                showActivePlayerHand(player)
                playersHand.isVisible = true
            }
        }

        playerComponents.add(activePlayer)
        contentPane.add(activePlayer)
    }

    fun showActivePlayerHand(player: Player){

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

    fun showPlayerHand(player: Player){

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

    fun showWithoutPossiblePositions(player: Player){

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

    fun showWithPossiblePositions(player: Player){

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
            if (!isAlreadyPlayed) {
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

    fun placePossiblePositions(grid: GridPane<ComponentView>, positions: List<Coordinate>, offsetX: Int, offsetY: Int){

        for ( coord in positions){

            val label = Label(
                width = 100,
                height = 100,
                visual = ColorVisual(Color.GREEN).apply { transparency = 0.7 }
            ).apply {
                onMouseClicked = {
                    if (chosenTile == null) throw IllegalStateException("Please, first select a Tile from the MoonWheel")
                    rootService.playerActionService.playTile(chosenTile!!.second, coord)
                    isAlreadyPlayed = true
                }
            }


            val x = coord.xCoord.toInt()
            val y = coord.yCoord.toInt()

            grid[x + offsetX, -y + offsetY] = label
        }
    }

    fun placeTiles(grid: GridPane<ComponentView>, player: Player, offsetX: Int, offsetY: Int){

        for (tile in player.tiles){

            val tileLabel = createTile(tile!!)
            val x = tile.position!!.xCoord.toInt()
            val y = tile.position!!.yCoord.toInt()

            grid[x + offsetX, -y + offsetY] = tileLabel
        }
    }

    fun addPlayers(game: NovaLunaGame){


        val grid = GridPane<ComponentView>(
            posX = 59,
            posY = 86,
            layoutFromCenter = false,
            rows = game.players.size,
            columns = 1,
            spacing = 20
        )

        for ((i,player) in game.players.withIndex()) {

            if (i == game.activePlayer){continue}

            val playerLabel = Button(
                width = 292,
                height = 199,
                text = "${player.playerName} : ${player.tokenCount} tokens left",
                alignment = Alignment.CENTER,
                font = Font(36, Color(0x000000), "Space Grotesk"),
                visual = ColorVisual(Color(0xC1780C)).apply { style.borderRadius = BorderRadius(10) }
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

    fun endTurn(){
        val overlayPane = Pane<ComponentView>(
            width = 1920,
            height = 1080,
            posX = 0,
            posY = 0,
            visual = ColorVisual(Color(0x8B570C)).apply { transparency = 0.5; style.borderRadius = BorderRadius(10)  }
        )

        val back = Label(
            width = 800,
            height = 500,
            posX = 560,
            posY = 290,
            visual = ColorVisual(Color(0xFFCC81)).apply { style.borderRadius = BorderRadius(10);style.borderRadius = BorderRadius(10)   }
        )

        val text = Label(
            posX = 820,
            posY = 333,
            width = 281,
            height = 61,
            text = "Next Player:",
            font = Font(48, Color.BLACK,"Space Grotesk" )
        )

        val playerName = Label(
            posX = 710,
            posY = 448,
            width = 500,
            height = 120,
            text = "Player1",
            alignment = Alignment.CENTER,
            font = Font(48, Color.BLACK,"Space Grotesk" ),
            visual = ColorVisual(Color(0xD39130)).apply { style.borderRadius = BorderRadius(10) }
        )

        overlayPane.addAll(back,text,playerName)
        contentPane.addAll(overlayPane)
        Thread.sleep(2000)
        contentPane.remove(overlayPane)

    }

}
