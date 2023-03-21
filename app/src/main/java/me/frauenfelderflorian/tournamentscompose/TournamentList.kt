package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var showInfo by rememberSaveable { mutableStateOf(false) }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = stringResource(R.string.app_title)) },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Routes.SETTINGS_EDITOR.route) }
                        ) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }
                        IconButton(onClick = { showInfo = true }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.about))
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(text = stringResource(R.string.new_tournament)) },
                    expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                    onClick = {
                        setCurrent(-1)
                        navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                    }
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                if (tournaments.isNotEmpty())
                    items(items = tournaments.sortedByDescending { it.start }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.clickable {
                                setCurrent(tournaments.indexOf(it))
                                navController.navigate(Routes.TOURNAMENT_VIEWER.route)
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.tournament_list_title,
                                    it.name,
                                    formatDate(it.start),
                                    formatDate(it.end)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            IconButton(onClick = {
                                setCurrent(tournaments.indexOf(it))
                                navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                            }
                        }
                    }
                else item {
                    Text(
                        text = stringResource(R.string.add_first_tournament_hint),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.ExtraLight
                    )
                }
            }
            if (showInfo) AlertDialog(
                onDismissRequest = { showInfo = false },
                icon = { Icon(Icons.Default.Info, null) },
                title = { Text("${stringResource(R.string.about)} ${stringResource(R.string.app_title)}") },
                text = { Text(stringResource(R.string.built_by_info)) },
                confirmButton = {
                    TextButton(onClick = { showInfo = false }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }
    }
}
