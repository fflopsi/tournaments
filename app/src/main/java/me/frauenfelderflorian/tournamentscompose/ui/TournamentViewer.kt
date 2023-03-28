package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.formatDate
import me.frauenfelderflorian.tournamentscompose.getTheme
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TournamentViewer(navController: NavController, theme: Int, tournament: Tournament) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    TournamentsTheme(getTheme(theme)) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.tournament_title, tournament.name)) },
                    navigationIcon = {
                        IconButton({ navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        IconButton({ navController.navigate(Routes.TOURNAMENT_EDITOR.route) }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = selectedTab == 0,
                    enter = scaleIn(),
                    exit = scaleOut(),
                ) {
                    ExtendedFloatingActionButton(
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text(stringResource(R.string.new_game)) },
                        expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                        onClick = {
                            tournament.updateCurrent(-1)
                            navController.navigate(Routes.GAME_EDITOR.route)
                        },
                    )
                }
            },
            contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TabRow(selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.details)) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.ranking)) },
                    )
                }
                val columnScope = this
                Box {
                    columnScope.AnimatedVisibility(
                        visible = selectedTab == 0,
                        enter = slideInHorizontally(initialOffsetX = { width -> -width }),
                        exit = slideOutHorizontally(targetOffsetX = { width -> -width }),
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (tournament.games.isNotEmpty()) {
                                items(tournament.games.sortedByDescending { it.date }) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            tournament.updateCurrent(tournament.games.indexOf(it))
                                            navController.navigate(Routes.GAME_VIEWER.route)
                                        },
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.game_title, formatDate(it.date)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(2f),
                                        )
                                        IconButton({
                                            tournament.updateCurrent(tournament.games.indexOf(it))
                                            navController.navigate(Routes.GAME_EDITOR.route)
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                stringResource(R.string.edit_game),
                                            )
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Text(
                                        text = stringResource(R.string.add_first_game_hint),
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.ExtraLight,
                                    )
                                }
                            }
                        }
                    }
                    columnScope.AnimatedVisibility(
                        visible = selectedTab == 1,
                        enter = slideInHorizontally(initialOffsetX = { width -> width }),
                        exit = slideOutHorizontally(targetOffsetX = { width -> width }),
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(tournament.playersByPoints) {
                                var menuExpanded by remember { mutableStateOf(false) }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(tournament.getPoints(it).toString())
                                    Text(
                                        text = it,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(2f),
                                    )
                                    Box {
                                        IconButton({ menuExpanded = true }) {
                                            Icon(Icons.Default.MoreVert, null)
                                        }
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.game_overview))
                                                },
                                                onClick = { /*TODO*/ },
                                                leadingIcon = {
                                                    Icon(Icons.Default.EmojiEvents, null)
                                                },
                                            )
                                            Divider()
                                            DropdownMenuItem(
                                                text = {
                                                    Text(stringResource(R.string.delete_player))
                                                },
                                                onClick = { /*TODO*/ },
                                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
