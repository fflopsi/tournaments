package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersEditor(
    navController: NavController,
    theme: Int,
    dynamicColor: Boolean,
    formerPlayers: String?,
) {
    val players = rememberMutableStateMapOf()
    var playersIdCounter by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (formerPlayers != null && formerPlayers.trim() != "") {
            for (player in formerPlayers.split(";")) players[playersIdCounter++] = player
        }
        players.entries.sortedBy { it.value }.forEach { players[it.key] = it.value }
    }

    TournamentsTheme(darkTheme = getTheme(theme), dynamicColor = dynamicColor) {
        val scope = rememberCoroutineScope()
        val hostState = remember { SnackbarHostState() }
        val scrollBehavior =
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        TopAppBarTitle(stringResource(R.string.player_editor_title), scrollBehavior)
                    },
                    navigationIcon = {
                        IconButton({ navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton({
                            for (player1 in players) {
                                if (player1.value.isBlank()) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(
                                                R.string.no_nameless_players
                                            )
                                        )
                                    }
                                    return@IconButton
                                }
                                for (player2 in players) {
                                    if (player1.key != player2.key && player1.value.trim() == player2.value.trim()) {
                                        scope.launch {
                                            hostState.showSnackbar(
                                                context.resources.getString(
                                                    R.string.no_same_name_players, player1.value
                                                )
                                            )
                                        }
                                        return@IconButton
                                    }
                                }
                            }
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "players", players.values.joinToString(";")
                            )
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = { // TODO: ExtendedFAB
                FloatingActionButton({ players[playersIdCounter++] = "" }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_new_player))
                }
            },
            snackbarHost = { SnackbarHost(hostState) },
            contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues),
            ) {
                items(players.entries.toList()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val context = LocalContext.current
                        TextField(
                            value = it.value,
                            onValueChange = { value ->
                                if (value.contains(";")) scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(
                                            R.string.no_semicolon_players
                                        )
                                    )
                                }
                                else {
                                    players[it.key] = value
                                }
                            },
                            singleLine = true,
                            label = { Text(stringResource(R.string.name)) },
                            placeholder = { Text(stringResource(R.string.name_unique)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(2f),
                        )
                        IconButton({ players.remove(it.key) }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete_player))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberMutableStateMapOf(
    vararg elements: Pair<Int, String>,
): SnapshotStateMap<Int, String> {
    return rememberSaveable(
        saver = listSaver(
            save = { map -> map.toList().map { "${it.first},${it.second}" } },
            restore = { list ->
                list.map { it.substringBefore(",").toInt() to it.substringAfter(",") }
                    .toMutableStateMap()
            },
        ),
    ) {
        elements.toList().map { it.first to it.second }.toMutableStateMap()
    }
}
