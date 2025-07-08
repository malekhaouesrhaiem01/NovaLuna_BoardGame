package gui

import entity.Player
import service.*
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
/**
 *  The ResultMenu Scene in NovaLuna.
 *
 *  Implements [Refreshable] to react to service layer updates.
 *
 *  @constructor Creates a new ResultMenu Scene with the specified rootService.
 *
 *  @param rootService The [RootService] that manages the game state.
 */
class ResultMenuScene (private val rootService: RootService) : MenuScene(1920, 1080), Refreshable {

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

    // This label is used to display the name of the winner
    private val winnerLabel = Label(
        text = "",
        width = 600,
        height = 200,
        posX = 660,
        posY = 160,
        alignment = Alignment.CENTER,
        font = Font(48, Color.WHITE,"Space Grotesk" ),
        visual = ColorVisual(Color(0xC1780C)).apply {
            style.borderRadius = BorderRadius(15)
        }
    )

    private val scoreLabels: List<Label> = List(4) { index ->
        Label(
            text = "",
            width = 600,
            height = 80,
            posX = 660,
            posY = 400 + index * 80,
            alignment = Alignment.CENTER,
            font = Font(48, Color(0xFFFFFFF), "JetBrains Mono"),
            visual = ColorVisual(Color(0xF9B44E))
        )
    }

    /**
     * Button to start a new game, triggers navigation back to the main menu.
     */
    val newGameButton = Button(
        text = "New Game",
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

    init{
        addComponents(contentPane)
        contentPane.addAll( winnerLabel, newGameButton, exitButton)
        scoreLabels.forEach { contentPane.add(it) }
    }

    /**
     * The refreshAfterGameEnd method is called by the service layer after a game has ended.
     * It sets the name of the winner.
     *
     * @param winner The [Player] who has won the game
     */
    override fun refreshAfterGameEnd(winner: Player) {
        val game = rootService.currentGame ?: return
        val players = game.players

        val minPoints = players.minOf { it.tokenCount }
        val winners = players.filter { it.tokenCount == minPoints }

        if (winners.size == 1) {
            // Eindeutiger Gewinner
            val winnerPlayer = winners.first()
            winnerLabel.text = "Winner: ${winnerPlayer.playerName} (${winnerPlayer.tokenCount})"

            // Zeige alle anderen Spieler
            val remainingPlayers = players.filter { it != winnerPlayer }
            remainingPlayers.forEachIndexed { index, player ->
                if (index < scoreLabels.size) {
                    scoreLabels[index].text = "${player.playerName}: ${player.tokenCount}"
                    scoreLabels[index].isVisible = true
                }
            }

            // Verbleibende Labels verstecken
            for (i in remainingPlayers.size until scoreLabels.size) {
                scoreLabels[i].isVisible = false
            }
        } else {
            // Unentschieden
            winnerLabel.text = "Tie!"

            // Zeige alle Spieler mit ihren Punkten
            players.forEachIndexed { index, player ->
                if (index < scoreLabels.size) {
                    scoreLabels[index].text = "${player.playerName}: ${player.tokenCount}"
                    scoreLabels[index].isVisible = true
                }
            }

            // Verbleibende Labels verstecken
            for (i in players.size until scoreLabels.size) {
                scoreLabels[i].isVisible = false
            }
        }
    }
}