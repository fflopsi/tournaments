package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.lifecycle.ViewModel
import java.util.UUID

actual class TournamentsModel : ViewModel() {
    actual var current: UUID? = null
    actual var tournaments = mapOf<UUID, TournamentWithGames>()
}

actual class PlayersModel : ViewModel() {
    actual val players = mutableListOf<String>()
    actual var edited = false
}
