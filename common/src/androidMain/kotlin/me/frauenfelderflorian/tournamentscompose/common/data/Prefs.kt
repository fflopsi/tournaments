package me.frauenfelderflorian.tournamentscompose.common.data

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
    private val experimentalFeaturesKey = booleanPreferencesKey("experimentalFeatures")
    private val playersKey = stringPreferencesKey("players")
    private val adaptivePointsKey = booleanPreferencesKey("adaptivePoints")
    private val firstPointsKey = intPreferencesKey("firstPoints")

    private var _theme by mutableStateOf(0)
    var theme
        set(value) = runBlocking {
            if (value < 0 || value > 2) {
                throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
            }
            launch { context.dataStore.edit { it[themeKey] = value } }
        }
        get() = _theme
    private var _dynamicColor by mutableStateOf(true)
    var dynamicColor
        set(value) = runBlocking {
            launch { context.dataStore.edit { it[dynamicColorKey] = value } }
        }
        get() = _dynamicColor
    private var _experimentalFeatures by mutableStateOf(false)
    var experimentalFeatures
        set(value) = runBlocking {
            launch { context.dataStore.edit { it[experimentalFeaturesKey] = value } }
        }
        get() = _experimentalFeatures
    private var _players by mutableStateOf(listOf<String>())
    var players
        set(value) = runBlocking {
            launch { context.dataStore.edit { it[playersKey] = value.joinToString(";") } }
        }
        get() = _players
    private var _adaptivePoints by mutableStateOf(true)
    var adaptivePoints
        set(value) = runBlocking {
            launch { context.dataStore.edit { it[adaptivePointsKey] = value } }
        }
        get() = _adaptivePoints
    private var _firstPoints by mutableStateOf(10)
    var firstPoints
        set(value) = runBlocking {
            launch { context.dataStore.edit { it[firstPointsKey] = value } }
        }
        get() = _firstPoints

    private val d = context.dataStore.data
    private val themeFlow = d.map { it[themeKey] ?: 0 }.distinctUntilChanged()
    private val dynamicColorFlow = d.map { it[dynamicColorKey] ?: true }.distinctUntilChanged()
    private val experimentalFeaturesFlow =
        d.map { it[experimentalFeaturesKey] ?: false }.distinctUntilChanged()
    private val playersFlow = d.map { it[playersKey] ?: "" }.distinctUntilChanged()
    private val adaptivePointsFlow = d.map { it[adaptivePointsKey] ?: true }.distinctUntilChanged()
    private val firstPointsFlow = d.map { it[firstPointsKey] ?: 10 }.distinctUntilChanged()

    /**
     * Initialize the [Prefs] values [theme], [dynamicColor], [players], [adaptivePoints] and
     * [firstPoints] with the values stored on disk and observe them for changes
     */
    @Composable
    fun Initialize() {
        _theme = themeFlow.asLiveData().observeAsState(theme).value
        _dynamicColor = dynamicColorFlow.asLiveData().observeAsState(dynamicColor).value
        _experimentalFeatures =
            experimentalFeaturesFlow.asLiveData().observeAsState(experimentalFeatures).value
        _players =
            playersFlow.asLiveData().observeAsState(players.joinToString(";")).value.split(";")
        if (players == listOf("")) _players = emptyList()
        _adaptivePoints = adaptivePointsFlow.asLiveData().observeAsState(adaptivePoints).value
        _firstPoints = firstPointsFlow.asLiveData().observeAsState(firstPoints).value
    }
}

class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
