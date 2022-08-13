package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(
    navController: NavHostController,
    theme: Int,
    tournaments: MutableList<Tournament>,
    setCurrent: (Int) -> Unit,
) {
    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = stringResource(R.string.app_title)) },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Routes.SETTINGS_EDITOR.route)
                        }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.about))
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    setCurrent(-1)
                    navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_new_tournament))
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (tournaments.isNotEmpty()) items(items = tournaments) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /*TournamentViewer*/ }
                        ) {
                            Text(
                                text = "\"${item.name}\" " +
                                        "from ${formatDate(item.start)} to ${formatDate(item.end)}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = {
                                setCurrent(tournaments.indexOf(item))
                                navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                            }) {
                                Icon(Icons.Default.Edit, "Edit tournament")
                            }
                        }
                    }
                    else item {
                        Text(
                            text = "Tap  +  in the lower right corner to add your first tournament",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.ExtraLight
                        )
                    }
                }
            }
        }
    }
}
