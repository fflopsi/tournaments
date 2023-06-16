package me.frauenfelderflorian.tournamentscompose.common.data

import com.google.gson.reflect.TypeToken
import me.frauenfelderflorian.tournamentscompose.common.ui.gson
import java.util.UUID

expect class TournamentsModel {
    var current: UUID?
    var tournaments: Map<UUID, TournamentWithGames>
}

expect class PlayersModel {
    val players: MutableList<String>
    var edited: Boolean
}

expect class TournamentWithGames(t: Tournament, games: List<Game>) {
    val t: Tournament
    val games: List<Game>
    var current: Game?
}

val TournamentWithGames.playersByPoints: List<String>
    get() = t.playersString.split(";").sortedByDescending { getPoints(it) }

fun TournamentWithGames.getPoints(player: String): Int {
    var points = 0
    for (game in games) {
        var present = 0
        for (player1 in t.playersString.split(";")) {
            if (game.ranking[player1]!! > 0) present++
        }
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

expect class Tournament(
    id: UUID,
    name: String,
    start: Long,
    end: Long,
    useAdaptivePoints: Boolean,
    firstPoints: Int = 10,
    playersString: String = "",
) {
    val id: UUID
    val name: String
    val start: Long
    val end: Long
    val useAdaptivePoints: Boolean
    val firstPoints: Int

    /**
     * Concatenated list of all players in this tournament. Use [players] to access.
     *
     * Leave this empty upon instantiation, and modify it afterwards using [players]
     */
    var playersString: String
}

/**
 * List of all players in this tournament. Use this to modify or read [Tournament.playersString]
 */
var Tournament.players: List<String>
    get() = playersString.split(";")
    set(value) {
        playersString = value.joinToString(";")
    }


expect class Game(
    id: UUID,
    tournamentId: UUID,
    date: Long,
    hoops: Int,
    hoopReached: Int,
    difficulty: String,
    rankingString: String = "",
) {
    val id: UUID
    val tournamentId: UUID
    val date: Long
    val hoops: Int
    val hoopReached: Int
    val difficulty: String

    /**
     * JSON version of the map of the ranking of this game. Use [ranking] to access
     *
     * Leave this empty upon instantiation, and modify it afterwards using [ranking]
     */
    var rankingString: String
}

/**
 * Map of the ranking of this game. Use this to modify or read [Game.rankingString]
 */
var Game.ranking: Map<String, Int>
    get() = gson.fromJson(rankingString, object : TypeToken<Map<String, Int>>() {}.type)
    set(value) {
        rankingString = gson.toJson(value)
    }
val Game.absentPlayers get() = ranking.filterValues { it == 0 }.keys
val Game.playersByRank: List<String>
    get() {
        val players = mutableListOf<String>()
        for (i in 0 until ranking.size - absentPlayers.size) {
            players.add(ranking.filterValues { it == i + 1 }.keys.first())
        }
        return players.toList()
    }

