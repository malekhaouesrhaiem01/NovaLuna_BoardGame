package service

import entity.NovaLunaGame
import service.bot.EasyBotService
import service.bot.HardBotService


/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService{
    /** The connected [GameService] for this rootService */
    var gameService = GameService(this)
    /** The connected [PlayerActionService] for this rootService */
    var playerActionService = PlayerActionService(this)
    /** The connected [TileLoader] for this rootService */
    val tileLoader = TileLoader()

    /** The connected [service.bot.EasyBotService] for this rootService */
    val easyBotService = EasyBotService(this)
    /** The connected [service.bot.HardBotService] for this rootService */
    val hardBotService = HardBotService(this)

    /**
     * The currently active [entity.KombiGame]. Can be `null`, if no game has started yet.
     */
    var currentGame: NovaLunaGame? = null

    /**
     * Adds the provided [newRefreshable] to all services connected to this root service
     *
     * @param newRefreshable The [Refreshable] to be added
     */
    private fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        easyBotService.addRefreshable(newRefreshable)
        hardBotService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}