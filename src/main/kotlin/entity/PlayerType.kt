package entity

import kotlinx.serialization.Serializable

/**
 * Represents the type of player.
 *
 * - [HUMAN]: A human player
 * - [EASYBOT] A basic level Bot Player
 * - [HARDBOT] A more challenging Bot Player
 */
@Serializable
enum class PlayerType
{
    HUMAN,
    EASYBOT,
    HARDBOT
}
