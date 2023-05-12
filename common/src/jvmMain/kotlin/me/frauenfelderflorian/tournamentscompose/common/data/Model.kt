package me.frauenfelderflorian.tournamentscompose.common.data

import com.google.gson.reflect.TypeToken
import java.util.UUID

actual data class TournamentWithGames actual constructor(
    actual val t: Tournament,
    actual val games: List<Game>,
) {
    actual var current: Game? = null
    actual val playersByPoints
        get() = t.playersString.split(";").sortedByDescending { getPoints(it) }

    actual fun getPoints(player: String): Int {
        var points = 0
        for (game in games) {
            var present = 0
            for (player1 in t.playersString.split(";")) if (game.ranking[player1]!! > 0) present++
            if (t.useAdaptivePoints) {
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
                    points += t.firstPoints - (present + 5)
                } else if (game.ranking[player] == 1) {
                    points += t.firstPoints
                } else if (game.ranking[player] == 2) {
                    points += t.firstPoints - 3
                } else if (game.ranking[player] == 3) {
                    points += t.firstPoints - 5
                } else if (game.ranking[player]!! > 3) {
                    points += t.firstPoints - (game.ranking[player]!! + 3)
                }
            }
        }
        return points
    }
}

actual data class Tournament actual constructor(
    actual val id: UUID,
    actual val name: String,
    actual val start: Long,
    actual val end: Long,
    actual val useAdaptivePoints: Boolean,
    actual val firstPoints: Int,
    /**
     * Concatenated list of all players in this tournament. Use [players] to access.
     *
     * Leave this empty upon instantiation, and modify it afterwards using [players]
     */
    actual var playersString: String,
) {
    /**
     * List of all players in this tournament. Use this to modify or read [playersString]
     */
    actual var players: List<String>
        get() = playersString.split(";")
        set(value) {
            playersString = value.joinToString(";")
        }
}

actual data class Game actual constructor(
    actual val id: UUID,
    actual val tournamentId: UUID,
    actual val date: Long,
    actual val hoops: Int,
    actual val hoopReached: Int,
    actual val difficulty: String,
    /**
     * JSON version of the map of the ranking of this game. Use [ranking] to access
     *
     * Leave this empty upon instantiation, and modify it afterwards using [ranking]
     */
    actual var rankingString: String,
) {
    /**
     * Map of the ranking of this game. Use this to modify or read [rankingString]
     */
    actual var ranking: Map<String, Int>
        get() = gson.fromJson(rankingString, object : TypeToken<Map<String, Int>>() {}.type)
        set(value) {
            rankingString = gson.toJson(value)
        }
    actual val absentPlayers get() = ranking.filterValues { it == 0 }.keys
    actual val playersByRank: List<String>
        get() {
            val players = mutableListOf<String>()
            for (i in 0 until ranking.size - absentPlayers.size) {
                players.add(ranking.filterValues { it == i + 1 }.keys.first())
            }
            return players.toList()
        }
}
