package service

import entity.*
import tools.aqua.bgw.util.Coordinate

/**
 * Implementiert den einfachen Bot.
 * Dieser wählt einen Zug zufällig aus allen möglichen aus.
 */
class EasyBot {
    /**
     * Führt den Zug des einfachen Bots aus.
     * Es wird zufällig ein möglicher Zug gewählt.
     * @return [Move] welchen der Bot diesen Zug spielt
     * @throws IllegalStateException wenn kein Zug gefunden werden konnte (sollte nicht passieren, wenn es Züge gibt).
     */
    fun playTileEasy(): Move {
        TODO("playTileEasy not yet implemented")
    }

    /**
     * Gibt eine Liste möglicher Tiles zurück, die vom Bot gewählt werden können.
     *
     * @return Eine Liste gültiger [Tile]-Objekte.
     */
    private fun possibleTiles(): List<Tile> {
        TODO("possibleTiles ist noch nicht implementiert.")
    }

    /**
     * Gibt eine Liste aller möglichen Positionen zurück, an denen ein Tile platziert werden kann.
     *
     * @return Eine Liste von [Coordinate]s.
     */
    private fun possiblePosition(): List<Coordinate> {
        TODO("possiblePosition ist noch nicht implementiert.")
    }
}