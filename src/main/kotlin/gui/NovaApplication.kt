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

    var gameScene = GameScene(rootService)
    var onlineGameScene = OnlineGameScene(rootService)

    private val mainMenuScene: MainMenuScene by lazy {
        MainMenuScene().apply {
            offlineButton.onMouseClicked = {
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
                    this@NovaApplication.showGameScene(gameScene)
                } catch (e: Exception) {
                    println("Failed to load game: ${e.message}")
                }
            }
        }
    }

    private val offlineMenuScene: OfflineMenuScene by lazy {
        OfflineMenuScene(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
    }

    private val joinGameSceneOne: JoinGameSceneOne by lazy {
        JoinGameSceneOne(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
    }

    private val joinGameSceneTwo: JoinGameSceneTwo by lazy {
        // Greift auf joinGameSceneOne.playerName zu, deshalb lazy
        JoinGameSceneTwo(rootService, playerName = joinGameSceneOne.playerName).apply {
            exitButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
    }

    val hostGameSceneOne: HostGameSceneOne by lazy {
        HostGameSceneOne(rootService).apply {
            backButton.onMouseClicked = {
                showMenuScene(mainMenuScene)
            }
        }
    }

    private val resultMenuScene: ResultMenuScene by lazy {
        ResultMenuScene(rootService).apply {
            newGameButton.onMouseClicked = {
                gameScene.ifOfflineMode = false
                onlineGameScene.ifOnlineMode = false
                showMenuScene(mainMenuScene)
            }
        }
    }

    init {
        // Register all components that need refresh callbacks
        mainMenuScene
        offlineMenuScene
        joinGameSceneOne
        joinGameSceneTwo
        hostGameSceneOne
        resultMenuScene

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