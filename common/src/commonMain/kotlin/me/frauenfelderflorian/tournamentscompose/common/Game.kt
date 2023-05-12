package me.frauenfelderflorian.tournamentscompose.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import java.util.UUID

val gson: Gson = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
val normalDp = 16.dp
val normalPadding = PaddingValues(normalDp, normalDp)

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

    /**
     * Map of the ranking of this game. Use this to modify or read [rankingString]
     */
    var ranking: Map<String, Int>
    val absentPlayers: Set<String>
    val playersByRank: List<String>
}
