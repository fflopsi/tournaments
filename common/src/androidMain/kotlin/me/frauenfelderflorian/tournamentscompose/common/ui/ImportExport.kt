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
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

fun exportToUri(
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
                context, MR.strings.exception_file.getString(context), Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: java.lang.Exception) {
        Toast.makeText(
            context, when (e) {
                is FileNotFoundException -> MR.strings.exception_file.getString(context)
                is IOException -> MR.strings.exception_io.getString(context)
                else -> MR.strings.exception.getString(context)
            }, Toast.LENGTH_SHORT
        ).show()
    }

}

fun importFromUri(
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
                context, MR.strings.exception_file.getString(context), Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: java.lang.Exception) {
        Toast.makeText(
            context, when (e) {
                is FileNotFoundException -> MR.strings.exception_file.getString(context)
                is JsonSyntaxException -> MR.strings.exception_json.getString(context)
                is IOException -> MR.strings.exception_io.getString(context)
                else -> MR.strings.exception.getString(context)
            }, Toast.LENGTH_SHORT
        ).show()
    }
}
