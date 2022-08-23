package me.frauenfelderflorian.tournamentscompose.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val THEME_KEY = intPreferencesKey("theme")
private val PLAYERS_KEY = stringPreferencesKey("players")

class Prefs(private val context: Context) : ViewModel() {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    var theme by mutableStateOf(0); private set
    val themeFlow = context.dataStore.data.map { it[THEME_KEY] ?: 0 }

    var players = mutableStateListOf<String>(); private set
    val playersFlow = context.dataStore.data.map { it[PLAYERS_KEY] ?: "" }

    fun updateTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        theme = newTheme
        launch {
            context.dataStore.edit { it[THEME_KEY] = newTheme }
        }
    }

    fun savePlayers(newPlayers: List<String>) = runBlocking {
        players.clear()
        newPlayers.forEach(players::add)
        launch {
            context.dataStore.edit { it[PLAYERS_KEY] = newPlayers.joinToString(";") }
        }
    }
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
