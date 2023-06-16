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
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.ranking
import me.frauenfelderflorian.tournamentscompose.common.ui.ChildStack
import me.frauenfelderflorian.tournamentscompose.common.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.ProvideComponentContext
import me.frauenfelderflorian.tournamentscompose.common.ui.Screen
import me.frauenfelderflorian.tournamentscompose.common.ui.theme.TournamentsTheme
import java.util.UUID

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
    val navigator = remember { StackNavigation<Screen>() }
    TournamentsTheme(
        darkTheme = when (prefs.theme) {
            1 -> false
            2 -> true
            else -> isSystemInDarkTheme()
        },
    ) {
        ChildStack(
            source = navigator,
            initialStack = { listOf(Screen.GameViewer) },
            handleBackButton = true,
        ) {
            when (it) {
                is Screen.GameViewer -> GameViewer(
                    navigator = navigator,
                    game = Game(
                        id = UUID.randomUUID(),
                        tournamentId = UUID.randomUUID(),
                        date = System.currentTimeMillis(),
                        hoops = 10,
                        hoopReached = 8,
                        difficulty = "very"
                    ).apply {
                        ranking = mapOf(
                            "best" to 1,
                            "second" to 2,
                            "third" to 3,
                            "fourth" to 4,
                            "last" to 5,
                            "absent1" to 0,
                            "absent2" to 0
                        )
                    },
                )

                else -> {}
            }
        }
    }
}
