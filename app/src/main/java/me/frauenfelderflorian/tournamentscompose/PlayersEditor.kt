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

    val players = rememberMutableStateMapOf()

    LaunchedEffect(Unit) {
        if (formerPlayers != null && formerPlayers.trim() != "")
            for (player in formerPlayers.split(";")) players[UUID.randomUUID()] = player
        players.entries.sortedBy { it.value }.forEach { players[it.key] = it.value }
    }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = "Edit Players") },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "players",
                                players.toMap()
                            )
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, "Save and exit")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { players[UUID.randomUUID()] = "" }) {
                    Icon(Icons.Default.Add, "Add a new player")
                }
            },
            snackbarHost = { SnackbarHost(hostState = hostState) }
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
                    items(items = players.entries.toList(), itemContent = { item ->
                        Row {
                            TextField(
                                value = item.value,
                                onValueChange = {
                                    if (it.contains(";"))
                                        scope.launch {
                                            hostState.showSnackbar(
                                                "No semicolon allowed in name",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    else players[item.key] = it
                                },
                                singleLine = true,
                                label = { Text("Name") },
                                placeholder = { Text(text = "Name of the player must be unique") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { players.remove(item.key) }) {
                                Icon(Icons.Default.Delete, "Delete player")
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun rememberMutableStateMapOf(vararg elements: Pair<UUID, String>): SnapshotStateMap<UUID, String> {
    return rememberSaveable(
        saver = listSaver(
            save = { stateMap -> stateMap.toList().map { "${it.first},${it.second}" } },
            restore = { it ->
                it.map { UUID.fromString(it.substringBefore(",")) to it.substringAfter(",") }
                    .toMutableStateMap()
            }
        )
    ) { elements.toList().map { it.first to it.second }.toMutableStateMap() }
}

