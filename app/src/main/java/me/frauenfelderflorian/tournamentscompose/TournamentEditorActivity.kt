package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor() {
    TournamentsComposeTheme {
        Scaffold(
            topBar = { SmallTopAppBar(title = { Text(text = "Edit Tournament") }) }
        ) { paddingValues: PaddingValues ->
            Text(text = "Edit here", modifier = Modifier.padding(paddingValues))
        }
    }
}
