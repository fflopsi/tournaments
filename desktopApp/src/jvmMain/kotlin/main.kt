import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.GameViewerContent
import me.frauenfelderflorian.tournamentscompose.common.TestComposable
import me.frauenfelderflorian.tournamentscompose.common.data.normalPadding

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var text by remember { mutableStateOf("Hello, World!") }
        Column {
            Button({ text = "Hello, Test" }) {
                Text(text)
            }
            TestComposable()
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
        }
    }
}