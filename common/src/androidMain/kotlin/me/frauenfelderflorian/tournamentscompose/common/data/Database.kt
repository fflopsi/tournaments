package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface TournamentDao {
    @Transaction
    @Query("SELECT * FROM tournament")
    fun getTournamentsWithGames(): LiveData<List<TournamentWithGames>>

    @Insert
    suspend fun insert(vararg tournaments: Tournament)

    @Update
    suspend fun update(vararg tournaments: Tournament)

    @Upsert
    suspend fun upsert(vararg tournaments: Tournament)

    @Delete
    suspend fun delete(tournament: Tournament)
}

@Dao
interface GameDao {
    @Insert
    suspend fun insert(vararg games: Game)

    @Update
    suspend fun update(vararg games: Game)

    @Upsert
    suspend fun upsert(vararg games: Game)

    @Delete
    suspend fun delete(game: Game)
}

@Database(entities = [Tournament::class, Game::class], version = 1)
abstract class TournamentsDatabase : RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun gameDao(): GameDao
}
