package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.runBlocking
import java.util.prefs.Preferences

actual class Prefs {
    @OptIn(ExperimentalSettingsApi::class)
    private val settings =
        PreferencesSettings(Preferences.userNodeForPackage(this::class.java)).toFlowSettings()
    private val themeKey = "theme"
    private val dynamicColorKey = "dynamicColor"
    private val experimentalFeaturesKey = "experimentalFeatures"
    private val playersKey = "players"
    private val adaptivePointsKey = "adaptivePoints"
    private val firstPointsKey = "firstPoints"

    private var _theme by mutableStateOf(0)

    @OptIn(ExperimentalSettingsApi::class)
    actual var theme: Int
        set(value) = runBlocking { settings.putInt(themeKey, value) }
        get() = _theme
    private var _dynamicColor by mutableStateOf(true)

    @OptIn(ExperimentalSettingsApi::class)
    actual var dynamicColor: Boolean
        set(value) = runBlocking { settings.putBoolean(dynamicColorKey, value) }
        get() = _dynamicColor
    private var _experimentalFeatures by mutableStateOf(false)

    @OptIn(ExperimentalSettingsApi::class)
    actual var experimentalFeatures: Boolean
        set(value) = runBlocking { settings.putBoolean(experimentalFeaturesKey, value) }
        get() = _experimentalFeatures
    private var _players by mutableStateOf(listOf<String>())

    @OptIn(ExperimentalSettingsApi::class)
    actual var players: List<String>
        set(value) = runBlocking { settings.putString(playersKey, value.joinToString(";")) }
        get() = _players
    private var _adaptivePoints by mutableStateOf(true)

    @OptIn(ExperimentalSettingsApi::class)
    actual var adaptivePoints: Boolean
        set(value) = runBlocking { settings.putBoolean(adaptivePointsKey, value) }
        get() = _adaptivePoints
    private var _firstPoints by mutableStateOf(10)

    @OptIn(ExperimentalSettingsApi::class)
    actual var firstPoints: Int
        set(value) = runBlocking { settings.putInt(firstPointsKey, value) }
        get() = _firstPoints

    @OptIn(ExperimentalSettingsApi::class)
    @Composable
    actual fun Initialize() {
        _theme = settings.getIntFlow(themeKey, 0).collectAsState(0).value
        _dynamicColor = settings.getBooleanFlow(dynamicColorKey, true).collectAsState(true).value
        _experimentalFeatures =
            settings.getBooleanFlow(experimentalFeaturesKey, false).collectAsState(false).value
        _players = settings.getStringFlow(playersKey, "").collectAsState("").value.split(";")
        _adaptivePoints =
            settings.getBooleanFlow(adaptivePointsKey, true).collectAsState(true).value
        _firstPoints = settings.getIntFlow(firstPointsKey, 10).collectAsState(10).value
    }
}