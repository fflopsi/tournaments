package me.frauenfelderflorian.tournamentscompose.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import me.frauenfelderflorian.tournamentscompose.TournamentsApp

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

    /**
     * Update the theme stored in the settings  to [newTheme]
     *
     * This will automatically also call [useTheme], if [TournamentsApp] was initialized correctly
     */
    fun saveTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        launch {
            context.dataStore.edit { it[THEME_KEY] = newTheme }
        }
    }

    /**
     * Use [newTheme] in the app, without changing the value stored in the settings
     */
    fun useTheme(newTheme: Int) {
        if (newTheme < 0 || newTheme > 2) throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        theme = newTheme
    }

    /**
     * Update the values stored in the settings to [newPlayers], [newAdaptivePoints],
     * [newFirstPoints]
     *
     * This will automatically also call [useSettings], if [TournamentsApp] was initialized
     * correctly
     */
    fun saveSettings(
        newPlayers: List<String> = players,
        newAdaptivePoints: Boolean = adaptivePoints,
        newFirstPoints: Int = firstPoints
    ) = runBlocking {
        if (newPlayers != players) launch {
            context.dataStore.edit { it[PLAYERS_KEY] = newPlayers.joinToString(";") }
        }
        if (newAdaptivePoints != adaptivePoints) launch {
            context.dataStore.edit { it[ADAPTIVE_POINTS_KEY] = newAdaptivePoints }
        }
        if (newFirstPoints != firstPoints) launch {
            context.dataStore.edit { it[FIRST_POINTS_KEY] = newFirstPoints }
        }
    }

    /**
     * Use [newPlayers], [newAdaptivePoints], [newFirstPoints] in the app, without changing the
     * value stored in the settings
     */
    fun useSettings(
        newPlayers: List<String> = players,
        newAdaptivePoints: Boolean = adaptivePoints,
        newFirstPoints: Int = firstPoints
    ) {
        if (newPlayers != players) players = newPlayers.toList() as SnapshotStateList<String>
        if (newAdaptivePoints != adaptivePoints) adaptivePoints = newAdaptivePoints
        if (newFirstPoints != firstPoints) firstPoints = newFirstPoints
    }
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
