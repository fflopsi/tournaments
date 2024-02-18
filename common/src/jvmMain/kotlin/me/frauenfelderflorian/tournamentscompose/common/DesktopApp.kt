package me.frauenfelderflorian.tournamentscompose.common

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import me.frauenfelderflorian.tournamentscompose.common.data.GameDaoTest
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDaoTest
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.common.ui.ProvideComponentContext
import me.frauenfelderflorian.tournamentscompose.common.ui.Screen
import me.frauenfelderflorian.tournamentscompose.common.ui.theme.TournamentsTheme

@OptIn(ExperimentalDecomposeApi::class)
fun desktopApp() {
    val lifecycle = LifecycleRegistry()
    val rootComponentContext = DefaultComponentContext(lifecycle)
    application {
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            CompositionLocalProvider(LocalScrollbarStyle provides defaultScrollbarStyle()) {
                ProvideComponentContext(rootComponentContext) { DesktopAppContent() }
            }
        }
    }
}

@Composable
fun DesktopAppContent() {
    val prefs = Prefs().apply { Initialize() }
    val tournamentDao = TournamentDaoTest()
    val gameDao = GameDaoTest()
    val tournamentsModel = TournamentsModel()
    val playersModel = PlayersModel()
    val navigator = remember { StackNavigation<Screen>() }
    TournamentsTheme(
        darkTheme = when (prefs.theme) {
            1 -> false
            2 -> true
            else -> isSystemInDarkTheme()
        },
    ) {
        TournamentStack(
            navigator = navigator,
            prefs = prefs,
            tournamentsModel = tournamentsModel,
            tournamentDao = tournamentDao,
            gameDao = gameDao,
            playersModel = playersModel,
        )
    }
}
