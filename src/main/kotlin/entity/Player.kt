package entity

import kotlinx.serialization.Serializable

/**
 * @property playerName The name of the Player.
 * @property tokenCount Number of available moon tokes the player can place.
 * @property moonTrackPosition The players current position on the time track.
 * @property onlineMode Shows if player is in online or offline mode.
 * @property playerType The type of player (human or bot)
 * @property playerColour The players assigned color.
 * @property tiles List of tiles placed by the player during the game.
 */
@Serializable
data class Player(val playerName : String,
                  var tokenCount: Int,
                  var moonTrackPosition: Int,
                  val onlineMode: Boolean,
                  val playerType: PlayerType,
                  val playerColour: PlayerColour,
                  val tiles: MutableList<Tile?> = mutableListOf(),
                  var height : Int
) {
    /**
     * Creates a deep copy of this player for use in bot simulations.
     * This method creates a new instance with deep copies of all mutable collections.
     * 
     * @return A deep copy of this player.
     */
    fun copy(): Player {
        return Player(
            playerName = this.playerName,
            tokenCount = this.tokenCount,
            moonTrackPosition = this.moonTrackPosition,
            onlineMode = this.onlineMode,
            playerType = this.playerType,
            playerColour = this.playerColour,
            tiles = this.tiles.map { it?.copy() }.toMutableList(),
            height = this.height
        )
    }
}
