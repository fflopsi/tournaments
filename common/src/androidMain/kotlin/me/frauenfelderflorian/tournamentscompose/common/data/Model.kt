package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.lifecycle.ViewModel
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

actual class TournamentsModel : ViewModel() {
    actual var current: UUID? = null
    actual var tournaments = mapOf<UUID, TournamentWithGames>()
}

actual class PlayersModel : ViewModel() {
    actual val players = mutableListOf<String>()
    actual var edited = false
}

actual data class TournamentWithGames actual constructor(
    @Embedded actual val t: Tournament,
    @Relation(parentColumn = "id", entityColumn = "tournamentId") actual val games: List<Game>,
) {
    @Ignore
    actual var current: Game? = null
}

@Entity
actual data class Tournament actual constructor(
    @PrimaryKey actual val id: UUID,
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
)

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
)
