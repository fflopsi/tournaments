package me.frauenfelderflorian.tournamentscompose.common.data

import kotlinx.coroutines.flow.Flow

actual interface TournamentDao {
    actual fun getTournamentsWithGames(): Flow<List<TournamentWithGames>>
    actual suspend fun insert(vararg tournaments: Tournament)
    actual suspend fun update(vararg tournaments: Tournament)
    actual suspend fun upsert(vararg tournaments: Tournament)
    actual suspend fun delete(tournament: Tournament)
}

class TournamentDaoTest : TournamentDao {
    override fun getTournamentsWithGames(): Flow<List<TournamentWithGames>> {
        TODO("Not yet implemented")
    }

    override suspend fun insert(vararg tournaments: Tournament) {
        TODO("Not yet implemented")
    }

    override suspend fun update(vararg tournaments: Tournament) {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(vararg tournaments: Tournament) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(tournament: Tournament) {
        TODO("Not yet implemented")
    }
}

actual interface GameDao {
    actual suspend fun insert(vararg games: Game)
    actual suspend fun update(vararg games: Game)
    actual suspend fun upsert(vararg games: Game)
    actual suspend fun delete(game: Game)
}

class GameDaoTest : GameDao {
    override suspend fun insert(vararg games: Game) {
        TODO("Not yet implemented")
    }

    override suspend fun update(vararg games: Game) {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(vararg games: Game) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(game: Game) {
        TODO("Not yet implemented")
    }
}

actual abstract class TournamentsDatabase {
    actual abstract fun tournamentDao(): TournamentDao
    actual abstract fun gameDao(): GameDao
}
