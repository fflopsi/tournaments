package me.frauenfelderflorian.tournamentscompose.common.ui

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames

fun exportToUri(
    uri: Uri?,
    context: Context,
    scope: CoroutineScope,
    hostState: SnackbarHostState,
    content: Any,
) {
    try {
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(gson.toJson(content, content::class.java).toByteArray())
                it.close()
            }
        } else {
            scope.launch { hostState.showSnackbar(MR.strings.exception_file.getString(context)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                when (e) {
                    is FileNotFoundException -> MR.strings.exception_file.getString(context)
                    is IOException -> MR.strings.exception_io.getString(context)
                    else -> MR.strings.exception.getString(context)
                }
            )
        }
    }

}

fun importFromUri(
    uri: Uri?,
    context: Context,
    scope: CoroutineScope,
    hostState: SnackbarHostState,
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
            scope.launch { hostState.showSnackbar(MR.strings.exception_file.getString(context)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                when (e) {
                    is FileNotFoundException -> MR.strings.exception_file.getString(context)
                    is JsonSyntaxException -> MR.strings.exception_json.getString(context)
                    is IOException -> MR.strings.exception_io.getString(context)
                    else -> MR.strings.exception.getString(context)
                }
            )
        }
    }
}
