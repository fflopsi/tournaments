package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
actual interface TournamentDao {
    @Transaction
    @Query("SELECT * FROM tournament")
    actual fun getTournamentsWithGames(): Flow<List<TournamentWithGames>>

    @Insert
    actual suspend fun insert(vararg tournaments: Tournament)

    @Update
    actual suspend fun update(vararg tournaments: Tournament)

    @Upsert
    actual suspend fun upsert(vararg tournaments: Tournament)

    @Delete
    actual suspend fun delete(tournament: Tournament)
}

@Dao
actual interface GameDao {
    @Insert
    actual suspend fun insert(vararg games: Game)

    @Update
    actual suspend fun update(vararg games: Game)

    @Upsert
    actual suspend fun upsert(vararg games: Game)

    @Delete
    actual suspend fun delete(game: Game)
}

@Database(entities = [Tournament::class, Game::class], version = 1)
actual abstract class TournamentsDatabase : RoomDatabase() {
    actual abstract fun tournamentDao(): TournamentDao
    actual abstract fun gameDao(): GameDao
}
