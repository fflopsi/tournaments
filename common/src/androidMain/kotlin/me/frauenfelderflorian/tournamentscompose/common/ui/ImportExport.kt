package me.frauenfelderflorian.tournamentscompose.common.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import tournamentscompose.common.generated.resources.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

@OptIn(ExperimentalResourceApi::class)
suspend fun exportToUri(
    uri: Uri?,
    context: Context,
    content: Any,
) {
    try {
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(gson.toJson(content, content::class.java).toByteArray())
                it.close()
            }
        } else {
            Toast.makeText(
                context, getString(Res.string.exception_file), Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: java.lang.Exception) {
        Toast.makeText(
            context, when (e) {
                is FileNotFoundException -> getString(Res.string.exception_file)
                is IOException -> getString(Res.string.exception_io)
                else -> getString(Res.string.exception)
            }, Toast.LENGTH_SHORT
        ).show()
    }

}

@OptIn(ExperimentalResourceApi::class)
suspend fun importFromUri(
    uri: Uri?,
    context: Context,
    scope: CoroutineScope,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
) {
    try {
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                gson.fromJson<Collection<TournamentWithGames>>(
                    BufferedReader(InputStreamReader(inputStream)).readText(),
                    object : TypeToken<Collection<TournamentWithGames>>() {}.type,
                ).forEach {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            tournamentDao.upsert(it.t)
                            gameDao.upsert(*it.games.toTypedArray())
                        }
                    }
                }
                inputStream.close()
            }
        } else {
            Toast.makeText(
                context, getString(Res.string.exception_file), Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: java.lang.Exception) {
        Toast.makeText(
            context, when (e) {
                is FileNotFoundException -> getString(Res.string.exception_file)
                is JsonSyntaxException -> getString(Res.string.exception_json)
                is IOException -> getString(Res.string.exception_io)
                else -> getString(Res.string.exception)
            }, Toast.LENGTH_SHORT
        ).show()
    }
}
