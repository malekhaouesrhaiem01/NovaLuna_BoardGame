package service

import entity.*


/**
 * Repräsentiert einen einzelnen Knoten im Monte-Carlo-Suchbaum.
 * Jeder Knoten speichert einen Spielzustand und die Statistik der Simulationen,
 * die durch ihn gelaufen sind.
 *
 * @property state Das [NovaLunaGame], den dieser Knoten repräsentiert.
 * @property parent Der Elternknoten im Baum. `null` für den Wurzelknoten.
 * @property moveThatLedHere Das [Tile], der vom Elternknoten zu diesem Zustand geführt hat.
 */
class MCTSNode(
    val state: NovaLunaGame,
    val parent: MCTSNode? = null,
    val moveThatLedHere: Move? = null,
    val untriedMoves: MutableList<Move>
) {
    var visits: Int = 0
    val scores: MutableMap<Int, Double> = mutableMapOf() // Map von Spieler-Index zu Score
    val children: MutableList<MCTSNode> = mutableListOf()

    /**
     * Gibt zurück, ob alle möglichen Züge von diesem Knoten aus bereits expandiert wurden.
     * @return `true`, wenn keine unversuchten Züge mehr vorhanden sind.
     */
    fun isFullyExpanded(): Boolean = untriedMoves.isEmpty()

    /**
     * Gibt zurück, ob der Spielzustand in diesem Knoten ein Endzustand ist.
     * @return `true`, wenn das Spiel in diesem Zustand beendet ist.
     */
    fun isTerminal(): Boolean {
        TODO("Implementiere eine Methode im NovaLunaGame, um zu prüfen, ob das Spiel vorbei ist.")
    }

    /**
     * Wählt das beste Kind dieses Knotens basierend auf der UCT-Formel (Upper Confidence Bound for Trees).
     *
     * @param explorationConstant Der Explorationsparameter C, der das Gleichgewicht zwischen
     *                            Ausnutzung (exploitation) und Erkundung (exploration) steuert.
     * @return Der vielversprechendste Kindknoten.
     */
    fun selectBestChild(explorationConstant: Double = 1.41): MCTSNode? {
        TODO("Berechne UCT-Wert für alle Kindknoten und gib den besten zurück.")
    }

}