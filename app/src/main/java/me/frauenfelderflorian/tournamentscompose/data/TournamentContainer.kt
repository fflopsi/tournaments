package me.frauenfelderflorian.tournamentscompose.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

//TODO: Consider packing TournamentContainer, Tournament and Game all in one file

class TournamentContainer : ViewModel() {
    var current = -1; private set
    val tournaments = mutableStateListOf<Tournament>()
    //val tournamentsByDate get() = tournaments.sortedByDescending { it.start }

    fun updateCurrent(new: Int) {
        if (new < -1 || new >= tournaments.size) throw ArrayIndexOutOfBoundsException()
        current = new
    }
}
