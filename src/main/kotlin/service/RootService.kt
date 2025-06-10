package service

import entity.NovaLunaGame


/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService{
    /** The connected [GameService] for this rootService */
    val gameService = GameService(this)
    /** The connected [PlayerActionService] for this rootService */
    val playerActionService = PlayerActionService(this)
    /** The connected [TileLoader] for this rootService */
    val tileLoader = TileLoader()

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
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}