package me.frauenfelderflorian.tournamentscompose.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("settings")

@SuppressLint("StaticFieldLeak")
class Prefs(private val context: Context) : ViewModel() {
    private val themeKey = intPreferencesKey("theme")
    private val dynamicColorKey = booleanPreferencesKey("dynamicColor")
    private val playersKey = stringPreferencesKey("players")
    private val adaptivePointsKey = booleanPreferencesKey("adaptivePoints")
    private val firstPointsKey = intPreferencesKey("firstPoints")

    var theme by mutableStateOf(0)
        private set
    var dynamicColor by mutableStateOf(true)
        private set
    var players = listOf<String>()
        private set
    var adaptivePoints by mutableStateOf(true)
        private set
    var firstPoints by mutableStateOf(10)
        private set

    private val d = context.dataStore.data
    private val themeFlow = d.map { it[themeKey] ?: 0 }.distinctUntilChanged()
    private val dynamicColorFlow = d.map { it[dynamicColorKey] ?: true }.distinctUntilChanged()
    private val playersFlow = d.map { it[playersKey] ?: "" }.distinctUntilChanged()
    private val adaptivePointsFlow = d.map { it[adaptivePointsKey] ?: true }.distinctUntilChanged()
    private val firstPointsFlow = d.map { it[firstPointsKey] ?: 10 }.distinctUntilChanged()

    /**
     * Initialize the [Prefs] values [theme], [dynamicColor], [players], [adaptivePoints] and
     * [firstPoints] with the values stored on disk and observe them for changes
     */
    @Composable
    fun Initialize() {
        theme = themeFlow.asLiveData().observeAsState(theme).value
        dynamicColor = dynamicColorFlow.asLiveData().observeAsState(dynamicColor).value
        players =
            playersFlow.asLiveData().observeAsState(players.joinToString(";")).value.split(";")
        adaptivePoints = adaptivePointsFlow.asLiveData().observeAsState(adaptivePoints).value
        firstPoints = firstPointsFlow.asLiveData().observeAsState(firstPoints).value
    }

    /** Update the theme stored in the settings to [newTheme] */
    fun saveTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) {
            throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        }
        launch { context.dataStore.edit { it[themeKey] = newTheme } }
    }

    /** Update the dynamicColor value stored in the settings to [newDynamicColor] */
    fun saveDynamicColor(newDynamicColor: Boolean) = runBlocking {
        launch { context.dataStore.edit { it[dynamicColorKey] = newDynamicColor } }
    }

    /**
     * Update the values stored in the settings to [newPlayers], [newAdaptivePoints],
     * [newFirstPoints]
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
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
