package me.frauenfelderflorian.tournamentscompose.data

import java.text.DateFormat
import java.util.*

class Tournament(
    var start: Date,
    var end: Date,
    val players: MutableList<String>,
    val useAdaptivePoints: Boolean,
    val firstPoints: Int = 10
) {
    var current = -1
    val games = mutableListOf<Game>()
    val currentGame get() = games[current]
    var name = ""
        set(value) {
            field = value.trim()
        }
    val dateRange get() = start..end
    val gamesByDate get() = games.sortedByDescending { it.date }
    val playersByPoints get() = players.sortedByDescending { getPoints(it) }

    fun getPoints(player: String): Int {
        var points = 0
        for (game in games) {
            var present = 0
            for (player1 in players) if (game.ranking[player1]!! > 0) present++
            if (useAdaptivePoints) {
                if (game.ranking[player] == 1) points += present + 5
                else if (game.ranking[player] == 2) points += present + 2
                else if (game.ranking[player] == 3) points += present
                else if (game.ranking[player]!! > 3) points += present - game.ranking[player]!! + 2
            } else {
                if (game.ranking[player] == 0) points += firstPoints - (present + 5)
                else if (game.ranking[player] == 1) points += firstPoints
                else if (game.ranking[player] == 2) points += firstPoints - 3
                else if (game.ranking[player] == 3) points += firstPoints - 5
                else if (game.ranking[player]!! > 3)
                    points += firstPoints - (game.ranking[player]!! + 3)
            }
        }
        return points
    }

    override fun toString() = "Tournament \"$name\" " +
            "from ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(start)} " +
            "to ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(end)}"
}
