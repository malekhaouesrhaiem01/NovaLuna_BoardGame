package entity

/**
 * @property playerName The name of the Player.
 * @property tokenCount Number of available moon tokes the player can place.
 * @property moonTrackPosition The players current position on the time track.
 * @property onlineMode Shows if player is in online or offline mode.
 * @property playerType The type of player (human or bot)
 * @property playerColour The players assigned color.
 * @property tiles List of tiles placed by the player during the game.
 */
data class Player(val playerName : String,
                  var tokenCount: Int,
                  var moonTrackPosition: Int,
                  val onlineMode: Boolean,
                  val playerType: PlayerType,
                  val playerColour: PlayerColour,
                  val tiles: MutableList<Tile> = mutableListOf(),
                  var height : Int
)
