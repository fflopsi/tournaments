package me.frauenfelderflorian.tournamentscompose.android.data

import androidx.lifecycle.ViewModel
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.Game

class TournamentsModel : ViewModel() {
    var current: UUID? = null
    var tournaments = mapOf<UUID, TournamentWithGames>()
}

data class TournamentWithGames(
    @Embedded val t: Tournament,
    @Relation(parentColumn = "id", entityColumn = "tournamentId") val games: List<Game>,
) {
    @Ignore
    var current: Game? = null

    val playersByPoints get() = t.playersString.split(";").sortedByDescending { getPoints(it) }

    fun getPoints(player: String): Int {
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

@Entity
data class Tournament(
    @PrimaryKey val id: UUID,
    val name: String,
    val start: Long,
    val end: Long,
    val useAdaptivePoints: Boolean,
    val firstPoints: Int = 10,
    /**
     * Concatenated list of all players in this tournament. Use [players] to access.
     *
     * Leave this empty upon instantiation, and modify it afterwards using [players]
     */
    var playersString: String = "",
) {
    /**
     * List of all players in this tournament. Use this to modify or read [playersString]
     */
    var players: List<String>
        get() = playersString.split(";")
        set(value) {
            playersString = value.joinToString(";")
        }
}
