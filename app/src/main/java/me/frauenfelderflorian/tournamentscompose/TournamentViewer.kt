package me.frauenfelderflorian.tournamentscompose

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
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
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TournamentViewer(
    navController: NavController,
    theme: Int,
    tournament: Tournament
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        var selectedPage by rememberSaveable { mutableStateOf(0) }
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.tournament_title, tournament.name))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = { //TODO: fix
                        IconButton(onClick = { navController.navigate(Routes.TOURNAMENT_EDITOR.route) }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = selectedPage == 0,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    ExtendedFloatingActionButton(
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text(stringResource(R.string.new_game)) },
                        expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                        onClick = {
                            tournament.updateCurrent(-1)
                            navController.navigate(Routes.GAME_EDITOR.route)
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar { //TODO: change to tabs
                    NavigationBarItem(
                        selected = selectedPage == 0,
                        icon = {
                            if (selectedPage == 0) Icon(Icons.Default.EmojiEvents, null)
                            else Icon(Icons.Outlined.EmojiEvents, null)
                        },
                        label = { Text(stringResource(R.string.games)) },
                        onClick = { selectedPage = 0 }
                    )
                    NavigationBarItem(
                        selected = selectedPage == 1,
                        icon = {
                            if (selectedPage == 1) Icon(Icons.Default.Person, null)
                            else Icon(Icons.Outlined.Person, null)
                        },
                        label = { Text(stringResource(R.string.leaderboard)) },
                        onClick = { selectedPage = 1 }
                    )
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            AnimatedVisibility(
                visible = selectedPage == 0,
                enter = slideInHorizontally(initialOffsetX = { width -> -width }),
                exit = slideOutHorizontally(targetOffsetX = { width -> -width })
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    if (tournament.games.isNotEmpty())
                        items(items = tournament.games.sortedByDescending { it.date }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    tournament.updateCurrent(tournament.games.indexOf(it))
                                    navController.navigate(Routes.GAME_VIEWER.route)
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.game_title, formatDate(it.date)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(2f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = {
                                    tournament.updateCurrent(tournament.games.indexOf(it))
                                    navController.navigate(Routes.GAME_EDITOR.route)
                                }) {
                                    Icon(Icons.Default.Edit, stringResource(R.string.edit_game))
                                }
                            }
                        }
                    else item {
                        Text(
                            text = stringResource(R.string.add_first_game_hint),
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.ExtraLight
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = selectedPage == 1,
                enter = slideInHorizontally(initialOffsetX = { width -> width }),
                exit = slideOutHorizontally(targetOffsetX = { width -> width })
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    items(items = tournament.playersByPoints) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = it + ": " + tournament.getPoints(it),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(Icons.Default.MoreHoriz, null)
                            }
                        }
                    }
                }
            }
        }
    }
}
