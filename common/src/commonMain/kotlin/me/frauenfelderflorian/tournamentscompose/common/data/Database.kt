package me.frauenfelderflorian.tournamentscompose.common.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import me.frauenfelderflorian.tournamentscompose.GameQueries
import me.frauenfelderflorian.tournamentscompose.TournamentQueries
import me.frauenfelderflorian.tournamentscompose.TournamentsDB
import java.nio.ByteBuffer
import java.util.UUID

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): TournamentsDB {
    return TournamentsDB(driverFactory.createDriver())
}

class TournamentDao(private val queries: TournamentQueries) {
    fun getTournaments(): Flow<List<Tournament>> {
        return queries.getTournaments { id, name, start, end, useAdaptivePoints, firstPoints, playersString ->
            Tournament(
                id = id.asUuid(),
                name = name,
                start = start,
                end = end,
                useAdaptivePoints = useAdaptivePoints.toInt() == 1,
                firstPoints = firstPoints.toInt(),
                playersString = playersString
            )
        }.asFlow().mapToList(Dispatchers.IO)
    }

    fun upsert(vararg tournaments: Tournament) {
        tournaments.forEach {
            queries.upsert(
                name = it.name,
                start = it.start,
                end = it.end,
                useAdaptivePoints = it.useAdaptivePoints.toInt().toLong(),
                firstPoints = it.firstPoints.toLong(),
                playersString = it.playersString,
                id = it.id.asByteArray()
            )
        }
    }

    fun delete(tournament: Tournament) {
        queries.delete(tournament.id.asByteArray())
    }
}

class GameDao(private val queries: GameQueries) {
    fun getGames(tournamentId: UUID): Flow<List<Game>> {
        return queries.getTournamentGames(
            tournamentId.asByteArray()
        ) { id, tId, date, hoops, hoopReached, difficulty, rankingString ->
            Game(
                id = id.asUuid(),
                tournamentId = tId.asUuid(),
                date = date,
                hoops = hoops.toInt(),
                hoopReached = hoopReached.toInt(),
                difficulty = difficulty,
                rankingString = rankingString
            )
        }.asFlow().mapToList(Dispatchers.IO)
    }

    fun upsert(vararg games: Game) {
        games.forEach {
            queries.upsert(
                date = it.date,
                hoops = it.hoops.toLong(),
                hoopReached = it.hoopReached.toLong(),
                difficulty = it.difficulty,
                rankingString = it.rankingString,
                id = it.id.asByteArray(),
                tournamentId = it.tournamentId.asByteArray()
            )
        }
    }

    fun delete(game: Game) {
        queries.delete(game.id.asByteArray())
    }
}

private fun UUID.asByteArray(): ByteArray =
    ByteBuffer.wrap(ByteArray(16)).putLong(this.mostSignificantBits)
        .putLong(this.leastSignificantBits).array()

private fun ByteArray.asUuid(): UUID =
    UUID(ByteBuffer.wrap(this).getLong(0), ByteBuffer.wrap(this).getLong(8))

private fun Boolean.toInt(): Int = if (this) 1 else 0
