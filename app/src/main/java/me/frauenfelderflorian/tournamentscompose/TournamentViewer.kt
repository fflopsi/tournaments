package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentViewer(
    navController: NavController,
    theme: Int,
    tournaments: MutableList<Tournament>,
    current: Int
) {
    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.tournament_title, tournaments[current].name))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {}
        }
    }
}