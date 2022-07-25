package me.frauenfelderflorian.tournamentscompose.data

import java.text.DateFormat
import java.util.*

class Game(var date: GregorianCalendar, var hoops: Int, var hoopReached: Int) {
    var difficulty = "not set"
        set(value) {
            field = value.trim()
        }
    val ranking = mutableMapOf<String, Int>()
    val absentPlayers get() = ranking.filterValues { it == 0 }.keys
    val playersByRank: List<String>
        get() {
            val players = mutableListOf<String>()
            for (i in 0 until ranking.size - absentPlayers.size)
                players.add(ranking.filterValues { it == i + 1 }.keys.first())
            return players.toList()
        }

    override fun toString() =
        "Game from ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)}, " +
                "\"$difficulty\" ($hoopReached/$hoops)"
}
