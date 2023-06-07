package me.frauenfelderflorian.tournamentscompose.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.defaultComponentContext
import me.frauenfelderflorian.tournamentscompose.common.TournamentsApp
import me.frauenfelderflorian.tournamentscompose.common.ui.ProvideComponentContext

class TournamentsAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootComponentContext = defaultComponentContext()
        setContent { ProvideComponentContext(rootComponentContext) { TournamentsApp(intent) } }
    }
}
