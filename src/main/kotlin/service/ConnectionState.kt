package service

/**
 * Enum representing all possible network and gameplay states for Nova Luna.
 * Used by [NetworkService] to control the connection and game lifecycle.
 */
enum class ConnectionState {

    /** No connection active. Initial state or after disconnect. */
    DISCONNECTED,

    /** Connected to the server, but not hosting or joining yet. */
    CONNECTED,

    /** Host has requested to create a game. Awaiting [//CreateGameResponse]. */
    WAITING_FOR_HOST_CONFIRMATION,

    /** Host is waiting for guest players to join. */
    WAITING_FOR_GUESTS,

    /** Client has requested to join a game. Awaiting [//JoinGameResponse]. */
    WAITING_FOR_JOIN_CONFIRMATION,

    /** Guest has joined and is waiting for the initial [//InitMessage] from the host. */
    WAITING_FOR_INIT,

    /** Game is running, and it is this player's turn to play. */
    PLAYING_MY_TURN,

    /** Game is running, but it's the opponent's turn. */
    WAITING_FOR_OPPONENT
}