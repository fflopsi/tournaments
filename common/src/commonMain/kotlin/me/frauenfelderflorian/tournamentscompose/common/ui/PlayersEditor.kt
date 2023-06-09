package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersEditor(
    navigator: StackNavigation<Screen>,
    playersModel: PlayersModel,
) {
    var playersIdCounter by rememberSaveable { mutableStateOf(0) }
    val players = rememberMutableStateMapOf(
        *playersModel.players.associateBy { playersIdCounter++ }.toList().toTypedArray()
    )

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(MR.strings.edit_players), scrollBehavior) },
                navigationIcon = { BackButton { navigator.pop() } },
                actions = {
                    val noNamelessPlayers = stringResource(MR.strings.no_nameless_players)
                    val noSameNamePlayers = stringResource(MR.strings.no_same_name_players)
                    IconButton({
                        for (player1 in players) {
                            if (player1.value.isBlank()) {
                                scope.launch { hostState.showSnackbar(noNamelessPlayers) }
                                return@IconButton
                            }
                            for (player2 in players) {
                                if (player1.key != player2.key && player1.value.trim() == player2.value.trim()) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            "$noSameNamePlayers ${player1.value}"
                                        )
                                    }
                                    return@IconButton
                                }
                            }
                        }
                        playersModel.players.clear()
                        playersModel.players.addAll(players.values)
                        playersModel.edited = true
                        navigator.pop()
                    }) {
                        Icon(Icons.Default.Check, stringResource(MR.strings.save_and_exit))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(MR.strings.add_new_player)) },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                onClick = { players[playersIdCounter++] = "" },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(players.toList()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(normalPadding),
                ) {
                    val invalidName = stringResource(MR.strings.invalid_name)
                    OutlinedTextField(
                        value = it.second,
                        onValueChange = { value ->
                            if (value.contains(";")) {
                                scope.launch { hostState.showSnackbar(invalidName) }
                            } else if (value.length < 100) {
                                players[it.first] = value
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(
                                "${stringResource(MR.strings.name)} ${
                                    players.toList().indexOf(it) + 1
                                }"
                            )
                        },
                        placeholder = { Text(stringResource(MR.strings.name_unique)) },
                        modifier = Modifier.fillMaxWidth().weight(2f),
                    )
                    IconButton({ players.remove(it.first) }) {
                        Icon(Icons.Default.Delete, stringResource(MR.strings.delete_player))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}
