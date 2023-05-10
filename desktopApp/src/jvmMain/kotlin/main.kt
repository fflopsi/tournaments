import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.frauenfelderflorian.tournamentscompose.common.TestComposable

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var text by remember { mutableStateOf("Hello, World!") }
        Column {
            Button({ text = "Hello, Test" }) {
                Text(text)
            }
            TestComposable()
        }
    }
}