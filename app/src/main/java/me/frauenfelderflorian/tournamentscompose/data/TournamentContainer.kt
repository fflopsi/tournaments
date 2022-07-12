package me.frauenfelderflorian.tournamentscompose.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

//TODO: Consider packing TournamentContainer, Tournament and Game all in one file

class TournamentContainer : ViewModel() {
    var current = -1
    val tournaments = mutableStateListOf<Tournament>()
    //val tournamentsByDate get() = tournaments.sortedByDescending { it.start }
}
