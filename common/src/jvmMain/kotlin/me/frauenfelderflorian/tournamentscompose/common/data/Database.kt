package me.frauenfelderflorian.tournamentscompose.common.data

import kotlinx.coroutines.flow.Flow

actual interface TournamentDao {
    actual fun getTournamentsWithGames(): Flow<List<TournamentWithGames>>
    actual suspend fun insert(vararg tournaments: Tournament)
    actual suspend fun update(vararg tournaments: Tournament)
    actual suspend fun upsert(vararg tournaments: Tournament)
    actual suspend fun delete(tournament: Tournament)
}

actual interface GameDao {
    actual suspend fun insert(vararg games: Game)
    actual suspend fun update(vararg games: Game)
    actual suspend fun upsert(vararg games: Game)
    actual suspend fun delete(game: Game)
}

actual abstract class TournamentsDatabase {
    actual abstract fun tournamentDao(): TournamentDao
    actual abstract fun gameDao(): GameDao
}
