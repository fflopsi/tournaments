package me.frauenfelderflorian.tournamentscompose.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class TournamentContainer : ViewModel() {
    var current = -1
        private set
    val tournaments = mutableStateListOf<Tournament>()

    fun updateCurrent(new: Int) {
        if (new < -1 || new >= tournaments.size) throw ArrayIndexOutOfBoundsException()
        current = new
    }
}

class Tournament(
    var start: Long,
    var end: Long,
    val players: MutableList<String>,
    val useAdaptivePoints: Boolean,
    val firstPoints: Int = 10,
) {
    var current = -1
    val games = mutableListOf<Game>()
    var name = ""
        set(value) {
            field = value.trim()
        }
    val playersByPoints get() = players.sortedByDescending { getPoints(it) }

    fun updateCurrent(new: Int) {
        if (new < -1 || new >= games.size) throw ArrayIndexOutOfBoundsException()
        current = new
    }

    fun getPoints(player: String): Int {
        var points = 0
        for (game in games) {
            var present = 0
            for (player1 in players) if (game.ranking[player1]!! > 0) present++
            if (useAdaptivePoints) {
                if (game.ranking[player] == 1) {
                    points += present + 5
                } else if (game.ranking[player] == 2) {
                    points += present + 2
                } else if (game.ranking[player] == 3) {
                    points += present
                } else if (game.ranking[player]!! > 3) {
                    points += present - game.ranking[player]!! + 2
                }
            } else {
                if (game.ranking[player] == 0) {
                    points += firstPoints - (present + 5)
                } else if (game.ranking[player] == 1) {
                    points += firstPoints
                } else if (game.ranking[player] == 2) {
                    points += firstPoints - 3
                } else if (game.ranking[player] == 3) {
                    points += firstPoints - 5
                } else if (game.ranking[player]!! > 3) {
                    points += firstPoints - (game.ranking[player]!! + 3)
                }
            }
        }
        return points
    }
}

class Game(var date: Long, var hoops: Int, var hoopReached: Int) {
    var difficulty = "not set"
        set(value) {
            field = value.trim()
        }
    val ranking = mutableMapOf<String, Int>()
    val absentPlayers get() = ranking.filterValues { it == 0 }.keys
    val playersByRank: List<String>
        get() {
            val players = mutableListOf<String>()
            for (i in 0 until ranking.size - absentPlayers.size) {
                players.add(ranking.filterValues { it == i + 1 }.keys.first())
            }
            return players.toList()
        }
}
