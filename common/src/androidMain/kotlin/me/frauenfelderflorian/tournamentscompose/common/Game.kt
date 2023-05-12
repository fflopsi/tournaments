package me.frauenfelderflorian.tournamentscompose.common

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import java.util.UUID

@Entity
actual data class Game actual constructor(
    @PrimaryKey actual val id: UUID,
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
