package me.frauenfelderflorian.tournamentscompose.data

//TODO: Consider packing TournamentContainer, Tournament and Game all in one file
//TODO: ViewModel or rememberSaveable (notifyListeners alternative)

class TournamentContainer {
    var current = -1
    val tournaments = mutableListOf<Tournament>()
    val size get() = tournaments.size
    val tournamentsByDate get() = tournaments.sortedByDescending { it.start }
    val currentTournament get() = tournaments[current]
    val currentGame get() = currentTournament.currentGame
}
