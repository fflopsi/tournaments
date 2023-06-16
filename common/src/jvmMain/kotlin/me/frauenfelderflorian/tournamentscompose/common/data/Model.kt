package me.frauenfelderflorian.tournamentscompose.common.data

import java.util.UUID

actual class TournamentsModel {
    actual var current: UUID? = null
    actual var tournaments = mapOf<UUID, TournamentWithGames>()
}

actual class PlayersModel {
    actual val players = mutableListOf<String>()
    actual var edited = false
}

actual data class TournamentWithGames actual constructor(
    actual val t: Tournament,
    actual val games: List<Game>,
) {
    actual var current: Game? = null
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
)

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
)
