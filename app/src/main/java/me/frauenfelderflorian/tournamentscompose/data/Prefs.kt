package me.frauenfelderflorian.tournamentscompose.data

import android.annotation.SuppressLint
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
import me.frauenfelderflorian.tournamentscompose.TournamentsApp

@SuppressLint("StaticFieldLeak")
class Prefs(private val context: Context) : ViewModel() {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val themeKey = intPreferencesKey("theme")
    private val playersKey = stringPreferencesKey("players")
    private val adaptivePointsKey = booleanPreferencesKey("adaptivePoints")
    private val firstPointsKey = intPreferencesKey("firstPoints")

    var theme by mutableStateOf(0)
        private set
    val themeFlow = context.dataStore.data.map { it[themeKey] ?: 0 }

    var players = mutableStateListOf<String>()
        private set
    val playersFlow = context.dataStore.data.map { it[playersKey] ?: "" }
    var adaptivePoints by mutableStateOf(true)
        private set
    val adaptivePointsFlow = context.dataStore.data.map { it[adaptivePointsKey] ?: true }
    var firstPoints by mutableStateOf(10)
        private set
    val firstPointsFlow = context.dataStore.data.map { it[firstPointsKey] ?: 10 }

    /**
     * Update the theme stored in the settings to [newTheme]
     *
     * This will automatically also call [useTheme], if [TournamentsApp] was initialized correctly
     */
    fun saveTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) {
            throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        }
        launch { context.dataStore.edit { it[themeKey] = newTheme } }
    }

    /**
     * Use [newTheme] in the app, without changing the value stored in the settings
     */
    fun useTheme(newTheme: Int) {
        if (newTheme < 0 || newTheme > 2) {
            throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        }
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
        newFirstPoints: Int = firstPoints,
    ) = runBlocking {
        if (newPlayers != players) {
            launch { context.dataStore.edit { it[playersKey] = newPlayers.joinToString(";") } }
        }
        if (newAdaptivePoints != adaptivePoints) {
            launch { context.dataStore.edit { it[adaptivePointsKey] = newAdaptivePoints } }
        }
        if (newFirstPoints != firstPoints) {
            launch { context.dataStore.edit { it[firstPointsKey] = newFirstPoints } }
        }
    }

    /**
     * Use [newPlayers], [newAdaptivePoints], [newFirstPoints] in the app, without changing the
     * value stored in the settings
     */
    fun useSettings(
        newPlayers: List<String> = players,
        newAdaptivePoints: Boolean = adaptivePoints,
        newFirstPoints: Int = firstPoints,
    ) {
        if (newPlayers != players) players = mutableStateListOf(*newPlayers.toTypedArray())
        if (newAdaptivePoints != adaptivePoints) adaptivePoints = newAdaptivePoints
        if (newFirstPoints != firstPoints) firstPoints = newFirstPoints
    }
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
