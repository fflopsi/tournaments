package me.frauenfelderflorian.tournamentscompose

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(navController: NavHostController, tournaments: MutableList<Tournament>) {
    TournamentsComposeTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = stringResource(R.string.app_title)) },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.about))
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                val context = LocalContext.current
                FloatingActionButton(onClick = {
                    Toast.makeText(
                        context,
                        "This will add a new tournament",
                        Toast.LENGTH_SHORT,
                    ).show()
                    navController.navigate(ROUTE_TOURNAMENT_EDITOR)
                }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_new_tournament))
                }
            }
        ) { paddingValues: PaddingValues ->
            TournamentList(tournaments, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun TournamentList(tournaments: MutableList<Tournament>, modifier: Modifier) {
    LazyColumn(modifier.fillMaxHeight()) {
        items(items = tournaments, itemContent = { item ->
            Text(text = item.name, modifier = Modifier.padding(8.dp))
        })
    }
}

