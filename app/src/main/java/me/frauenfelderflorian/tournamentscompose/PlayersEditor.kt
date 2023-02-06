package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersEditor(navController: NavController, theme: Int, formerPlayers: String?) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val players = rememberMutableStateMapOf()

    LaunchedEffect(Unit) {
        if (formerPlayers != null && formerPlayers.trim() != "")
            for (player in formerPlayers.split(";")) players[UUID.randomUUID()] = player
        players.entries.sortedBy { it.value }.forEach { players[it.key] = it.value }
    }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = { Text(text = stringResource(R.string.player_editor_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton(onClick = {
                            for (player1 in players) {
                                if (player1.value.isBlank()) {
                                    scope.launch {
                                        hostState.showSnackbar(context.resources.getString(R.string.no_nameless_players))
                                    }
                                    return@IconButton
                                }
                                for (player2 in players) {
                                    if (player1.key != player2.key
                                        && player1.value.trim() == player2.value.trim()
                                    ) {
                                        scope.launch {
                                            hostState.showSnackbar(
                                                context.resources.getString(
                                                    R.string.no_same_name_players,
                                                    player1.value
                                                )
                                            )
                                        }
                                        return@IconButton
                                    }
                                }
                            }
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "players",
                                players.values.joinToString(";")
                            )
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { players[UUID.randomUUID()] = "" }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_new_player))
                }
            },
            snackbarHost = { SnackbarHost(hostState = hostState) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(items = players.entries.toList(), itemContent = {
                    Row {
                        val context = LocalContext.current
                        TextField(
                            value = it.value,
                            onValueChange = { value ->
                                if (value.contains(";"))
                                    scope.launch {
                                        hostState.showSnackbar(context.resources.getString(R.string.no_semicolon_players))
                                    }
                                else players[it.key] = value
                            },
                            singleLine = true,
                            label = { Text(stringResource(R.string.name)) },
                            placeholder = { Text(stringResource(R.string.name_unique)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(2f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { players.remove(it.key) }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete_player))
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun rememberMutableStateMapOf(vararg elements: Pair<UUID, String>): SnapshotStateMap<UUID, String> {
    return rememberSaveable(
        saver = listSaver(
            save = { map -> map.toList().map { "${it.first},${it.second}" } },
            restore = { list ->
                list.map { UUID.fromString(it.substringBefore(",")) to it.substringAfter(",") }
                    .toMutableStateMap()
            }
        )
    ) { elements.toList().map { it.first to it.second }.toMutableStateMap() }
}

