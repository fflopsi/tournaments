package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

class TournamentListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TournamentsComposeTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//
//                }
            }
        }
    }
}
