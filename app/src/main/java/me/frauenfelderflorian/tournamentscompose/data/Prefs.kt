package me.frauenfelderflorian.tournamentscompose.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
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
private val ADAPTIVE_POINTS_KEY = booleanPreferencesKey("adaptivePoints")
private val FIRST_POINTS_KEY = intPreferencesKey("firstPoints")

class Prefs(private val context: Context) : ViewModel() {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    var theme by mutableStateOf(0); private set
    val themeFlow = context.dataStore.data.map { it[THEME_KEY] ?: 0 }

    var players = mutableStateListOf<String>(); private set
    val playersFlow = context.dataStore.data.map { it[PLAYERS_KEY] ?: "" }
    var adaptivePoints by mutableStateOf(true); private set
    val adaptivePointsFlow = context.dataStore.data.map { it[ADAPTIVE_POINTS_KEY] ?: true }
    var firstPoints by mutableStateOf(10); private set
    val firstPointsFlow = context.dataStore.data.map { it[FIRST_POINTS_KEY] ?: 10 }

    fun updateTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        theme = newTheme
        launch {
            context.dataStore.edit { it[THEME_KEY] = newTheme }
        }
    }

    fun save(
        newPlayers: List<String> = players,
        newAdaptivePoints: Boolean = adaptivePoints,
        newFirstPoints: Int = firstPoints
    ) {
        if (newPlayers != players)
            runBlocking {
                players.clear()
                newPlayers.forEach(players::add)
                launch {
                    context.dataStore.edit { it[PLAYERS_KEY] = newPlayers.joinToString(";") }
                }
            }
        if (newAdaptivePoints != adaptivePoints)
            runBlocking {
                adaptivePoints = newAdaptivePoints
                launch {
                    context.dataStore.edit { it[ADAPTIVE_POINTS_KEY] = newAdaptivePoints }
                }
            }
        if (newFirstPoints != firstPoints)
            runBlocking {
                firstPoints = newFirstPoints
                launch {
                    context.dataStore.edit { it[FIRST_POINTS_KEY] = newFirstPoints }
                }
            }
    }
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
