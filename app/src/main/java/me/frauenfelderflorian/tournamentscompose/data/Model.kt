package me.frauenfelderflorian.tournamentscompose.data

import androidx.lifecycle.ViewModel
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import java.util.UUID

class TournamentsModel : ViewModel() {
    var current = -1
        private set
    var tournaments = listOf<TournamentWithGames>()

    fun updateCurrent(new: Int) {
        if (new < -1 || new >= tournaments.size) throw ArrayIndexOutOfBoundsException()
        current = new
    }
}

data class TournamentWithGames(
    @Embedded val t: Tournament,
    @Relation(parentColumn = "id", entityColumn = "tournamentId") val games: List<Game>,
) {
    @Ignore
    var current: Int = -1

    val playersByPoints get() = t.players.split(";").sortedByDescending { getPoints(it) }

    fun updateCurrent(new: Int) {
        if (new < -1 || new >= games.size) throw ArrayIndexOutOfBoundsException()
        current = new
    }

    fun getPoints(player: String): Int {
        var points = 0
        for (game in games) {
            var present = 0
            for (player1 in t.players.split(";")) if (game.rankingMap[player1]!! > 0) present++
            if (t.useAdaptivePoints) {
                if (game.rankingMap[player] == 1) {
                    points += present + 5
                } else if (game.rankingMap[player] == 2) {
                    points += present + 2
                } else if (game.rankingMap[player] == 3) {
                    points += present
                } else if (game.rankingMap[player]!! > 3) {
                    points += present - game.rankingMap[player]!! + 2
                }
            } else {
                if (game.rankingMap[player] == 0) {
                    points += t.firstPoints - (present + 5)
                } else if (game.rankingMap[player] == 1) {
                    points += t.firstPoints
                } else if (game.rankingMap[player] == 2) {
                    points += t.firstPoints - 3
                } else if (game.rankingMap[player] == 3) {
                    points += t.firstPoints - 5
                } else if (game.rankingMap[player]!! > 3) {
                    points += t.firstPoints - (game.rankingMap[player]!! + 3)
                }
            }
        }
        return points
    }
}

@Entity
data class Tournament(
    @PrimaryKey val id: UUID,
    val start: Long,
    val end: Long,
    val useAdaptivePoints: Boolean,
    val firstPoints: Int = 10,
) {
    var players = ""
    var name = ""
        set(value) {
            field = value.trim()
        }
    var playersList: List<String>
        get() = players.split(";")
        set(value) {
            players = value.joinToString(";")
        }
}

@Entity
data class Game(
    @PrimaryKey val id: UUID,
    val tournamentId: UUID,
    val date: Long,
    val hoops: Int,
    val hoopReached: Int,
) {
    var ranking = ""
    var difficulty = "not set"
        set(value) {
            field = value.trim()
        }
    var rankingMap: Map<String, Int>
        get() {
            return GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
                .fromJson(ranking, object : TypeToken<MutableMap<String, Int>>() {}.type)
        }
        set(value) {
            ranking =
                GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
                    .toJson(value)
        }
    val absentPlayers get() = rankingMap.filterValues { it == 0 }.keys
    val playersByRank: List<String>
        get() {
            val players = mutableListOf<String>()
            for (i in 0 until rankingMap.size - absentPlayers.size) {
                players.add(rankingMap.filterValues { it == i + 1 }.keys.first())
            }
            return players.toList()
        }
}
