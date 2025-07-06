package service

import entity.Player

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable{
    /**
     * perform refreshes that are necessary after a Players Turn starts
     */
    fun refreshAfterStartTurn() {}

    /**
     * perform refreshes that are necessary after a Players Turn ends
     */
    fun refreshAfterEndTurn() {}

    /**
     * perform refreshes that are necessary after a Game is started
     */
    fun refreshAfterStartGame() {}

    /**
     * preform refreshes that are necessary after a Tile is played
     */
    fun refreshAfterTilePlayed() {}

    /**
     * perform refreshes that are necessary after the gameState is undone
     */
    fun refreshAfterUndo() {}

    /**
     * perform refreshes that are necessary after the gameState is redone
     */
    fun refreshAfterRedo() {}

    /**
     * preform refreshes that are necessary after the moonWheel has been refilled
     */
    fun refreshAfterRefill() {}

    /**
     * perform refresh that are necessary after a player ragequits (leaves mid-game)
     */
    fun refreshAfterRageQuit() {}

    /**
     * perform refreshes that are necessary after a NovaLuna Game ends
     * @param winner The winner of the NovaLuna Game
     */
    fun refreshAfterGameEnd(winner : Player){}
    // fun refreshAfterMoveMeepleAndPlayer()
    /**
     * refreshes the network connection status with the given information
     *
     * @param state the information to show
     */
    fun refreshConnectionState(state: ConnectionState) {}

    /**
     * perform refreshes that are necessary after a Player joins the Game
     */
    fun refreshAfterPlayerJoined() {}
}
