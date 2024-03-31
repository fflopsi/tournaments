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
