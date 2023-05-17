package me.frauenfelderflorian.tournamentscompose.android.ui

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
import me.frauenfelderflorian.tournamentscompose.android.R
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.ui.gson

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
            scope.launch { hostState.showSnackbar(context.getString(R.string.exception_file)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                context.getString(
                    when (e) {
                        is FileNotFoundException -> R.string.exception_file
                        is IOException -> R.string.exception_io
                        else -> R.string.exception
                    }
                )
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
            scope.launch { hostState.showSnackbar(context.getString(R.string.exception_file)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                context.getString(
                    when (e) {
                        is FileNotFoundException -> R.string.exception_file
                        is JsonSyntaxException -> R.string.exception_json
                        is IOException -> R.string.exception_io
                        else -> R.string.exception
                    }
                )
            )
        }
    }
}
