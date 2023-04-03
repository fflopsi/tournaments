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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersEditor(
    navController: NavController,
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

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(R.string.edit_players), scrollBehavior) },
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
                                        context.resources.getString(R.string.no_nameless_players)
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.add_new_player)) },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                onClick = { players[playersIdCounter++] = "" },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val context = LocalContext.current
                    TextField(
                        value = it.value,
                        onValueChange = { value ->
                            if (value.contains(";")) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(R.string.invalid_name)
                                    )
                                }
                            } else if (value.length < 100) {
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
