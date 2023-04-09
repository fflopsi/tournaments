package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.util.UUID
import kotlin.reflect.KFunction1
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.TournamentWithGames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(
    navController: NavHostController,
    tournaments: Map<UUID, TournamentWithGames>,
    setCurrent: KFunction1<UUID?, Unit>,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(R.string.app_title), scrollBehavior) },
                actions = {
                    IconButton({ navController.navigate(Routes.SETTINGS_EDITOR.route) }) {
                        Icon(Icons.Default.Settings, stringResource(R.string.settings))
                    }
                    IconButton({ showInfo.value = true }) {
                        Icon(Icons.Outlined.Info, stringResource(R.string.about))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.new_tournament)) },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                onClick = {
                    setCurrent(null)
                    navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                },
            )
        },
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            if (tournaments.isNotEmpty()) {
                items(tournaments.values.sortedByDescending { it.t.start }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                setCurrent(it.t.id)
                                navController.navigate(Routes.TOURNAMENT_VIEWER.route)
                            }
                            .padding(normalPadding),
                    ) {
                        Column(Modifier.weight(2f)) {
                            Text(text = it.t.name, style = titleStyle)
                            Text(
                                text = "${formatDate(it.t.start)} ${stringResource(R.string.to)} ${
                                    formatDate(it.t.end)
                                }, ${it.games.size} ${
                                    stringResource(
                                        if (it.games.size == 1) R.string.game else R.string.games
                                    )
                                }",
                                style = detailsStyle,
                            )
                        }
                        IconButton({
                            setCurrent(it.t.id)
                            navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                        }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.add_first_tournament_hint),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(normalPadding),
                    )
                }
            }
        }
        InfoDialog(showDialog = showInfo)
    }
}
