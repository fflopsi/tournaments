package me.frauenfelderflorian.tournamentscompose.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.ui.GameViewerContent
import me.frauenfelderflorian.tournamentscompose.common.ui.normalPadding
import moe.tlaster.precompose.PreComposeWindow
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

fun desktopApp() = application {
    PreComposeWindow(onCloseRequest = ::exitApplication) {
        DesktopAppContent()
    }
}

@Composable
fun DesktopAppContent() {
    val navigator = rememberNavigator()
    NavHost(navigator = navigator, initialRoute = "/home") {
        scene(route = "/home") {
            Button({ navigator.navigate("/game") }) {
                Text("Hello, World! Go to Game")
            }
        }
        scene(route = "/game") {
            Column {
                GameViewerContent(
                    game = Game(
                        id = UUID.randomUUID(),
                        tournamentId = UUID.randomUUID(),
                        date = 0,
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
                    modifier = Modifier.padding(normalPadding),
                )
                Button({ navigator.goBack() }) {
                    Text("Back")
                }
            }
        }
    }
}
