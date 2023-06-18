package me.frauenfelderflorian.tournamentscompose.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import me.frauenfelderflorian.tournamentscompose.common.androidApp

class TournamentsAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidApp(this)
    }
}
