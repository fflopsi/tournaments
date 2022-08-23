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
                            for (player1 in players) {
                                if (player1.value.isBlank()) {
                                    scope.launch {
                                        hostState.showSnackbar("Nameless players not allowed")
                                    }
                                    return@IconButton
                                }
                                for (player2 in players) {
                                    if (player1.key != player2.key
                                        && player1.value.trim() == player2.value.trim()
                                    ) {
                                        scope.launch {
                                            hostState.showSnackbar("Two players cannot have the same name: ${player1.value}")
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
                    items(items = players.entries.toList(), itemContent = {
                        Row {
                            TextField(
                                value = it.value,
                                onValueChange = { value ->
                                    if (value.contains(";"))
                                        scope.launch {
                                            hostState.showSnackbar("No semicolon allowed in name")
                                        }
                                    else players[it.key] = value
                                },
                                singleLine = true,
                                label = { Text("Name") },
                                placeholder = { Text(text = "Name of the player must be unique") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { players.remove(it.key) }) {
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
            save = { map -> map.toList().map { "${it.first},${it.second}" } },
            restore = { list ->
                list.map { UUID.fromString(it.substringBefore(",")) to it.substringAfter(",") }
                    .toMutableStateMap()
            }
        )
    ) { elements.toList().map { it.first to it.second }.toMutableStateMap() }
}

