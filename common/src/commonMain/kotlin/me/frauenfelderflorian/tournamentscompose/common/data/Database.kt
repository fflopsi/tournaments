package me.frauenfelderflorian.tournamentscompose.common.data

import kotlinx.coroutines.flow.Flow

expect interface TournamentDao {
    fun getTournamentsWithGames(): Flow<List<TournamentWithGames>>
    suspend fun insert(vararg tournaments: Tournament)
    suspend fun update(vararg tournaments: Tournament)
    suspend fun upsert(vararg tournaments: Tournament)
    suspend fun delete(tournament: Tournament)
}

expect interface GameDao {
    suspend fun insert(vararg games: Game)
    suspend fun update(vararg games: Game)
    suspend fun upsert(vararg games: Game)
    suspend fun delete(game: Game)
}

expect abstract class TournamentsDatabase {
    abstract fun tournamentDao(): TournamentDao
    abstract fun gameDao(): GameDao
}
