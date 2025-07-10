package gui

import entity.Player
import tools.aqua.bgw.core.BoardGameApplication
import service.Refreshable
import service.RootService

/**
 * Implementation of the BGW [BoardGameApplication] for the game "NovaLuna"
 */
object NovaApplication : BoardGameApplication("NovaLuna"), Refreshable {

    private val rootService = RootService()

    private var mainMenuScene: MainMenuScene
    private lateinit var offlineMenuScene: OfflineMenuScene
    private lateinit var joinGameSceneOne: JoinGameSceneOne
    private lateinit var joinGameSceneTwo: JoinGameSceneTwo
    lateinit var hostGameSceneOne: HostGameSceneOne
    private val resultMenuScene: ResultMenuScene

    var gameScene = GameScene(rootService)
    var onlineGameScene = OnlineGameScene(rootService)

    init {
        // Register all components that need refresh callbacks

        mainMenuScene = MainMenuScene().apply {
            offlineButton.onMouseClicked = {
                // Reset the offline menu state when navigating to it
                offlineMenuScene.resetMenuState()
                showMenuScene(offlineMenuScene)
            }
            joinButton.onMouseClicked = {
                showMenuScene(joinGameSceneOne)
            }
            hostButton.onMouseClicked = {
                showMenuScene(hostGameSceneOne)
            }
            loadButton.onMouseClicked = {
                try {
                    rootService.playerActionService.load()
                    // If loading is successful, show the game scene
                    this@NovaApplication.showGameScene(gameScene)
                } catch (e: Exception) {
                    // If loading fails, we could show an error - for now just print to console
                    println("Failed to load game: ${e.message}")
                }
            }
        }
        offlineMenuScene = OfflineMenuScene(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
        joinGameSceneOne = JoinGameSceneOne(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
        joinGameSceneTwo = JoinGameSceneTwo(rootService, playerName = joinGameSceneOne.playerName).apply {
            exitButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
        hostGameSceneOne = HostGameSceneOne(rootService).apply {

            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
        resultMenuScene = ResultMenuScene(rootService).apply{
            newGameButton.onMouseClicked = {
                gameScene.ifOfflineMode = false
                onlineGameScene.ifOnlineMode = false
                showMenuScene(mainMenuScene)
            }
        }

        gameScene = GameScene(rootService).apply{
            rageQuitButton.onMouseClicked = {
                val game = rootService.currentGame

                when {
                    game == null -> {
                        println("No game available.")
                    }
                    game.players.isEmpty() || game.activePlayer !in game.players.indices -> {
                        println("No valid active player.")
                    }
                    else -> {
                        // Player who quits
                        val quitter = game.players[game.activePlayer]
                        println("${quitter.playerName} has quit.")

                        // Remove the quitting player
                        game.players.removeAt(game.activePlayer)

                        // Adjust active player index
                        game.activePlayer = game.activePlayer % game.players.size

                        // Determine the winner: lowest token count
                        val winner = game.players.minByOrNull { it.tokenCount }
                            ?: throw IllegalStateException("At least one player must remain.")

                        // End the game and show the result scene
                        rootService.gameService.endGame(winner)
                    }
                }
            }

        }

        rootService.addRefreshables(
            this,
            mainMenuScene,
            offlineMenuScene,
            joinGameSceneOne,
            joinGameSceneTwo,
            resultMenuScene,
            gameScene,
            onlineGameScene
        )

        this.showMenuScene(mainMenuScene)

    }

    override fun refreshAfterStartGame() {
        hideMenuScene(500)
    }

    override fun refreshAfterGameEnd(winner: Player) {
        resultMenuScene.refreshAfterGameEnd(winner)
        showMenuScene(resultMenuScene)
    }
}