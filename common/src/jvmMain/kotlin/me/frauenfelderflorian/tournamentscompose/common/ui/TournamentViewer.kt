package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.data.players
import me.frauenfelderflorian.tournamentscompose.common.data.ranking

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
actual fun TournamentViewer(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState()
    val showInfo = remember { mutableStateOf(false) }
    var showNewPlayerDialog by remember { mutableStateOf(false) }
    val showDeletePlayerDialog = remember { mutableStateOf(false) }
    val playerToBeDeleted = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(MR.strings.tournament_title, tournament.t.name),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    IconButton({ navigator.push(Screen.TournamentEditor) }) {
                        Icon(Icons.Default.Edit, stringResource(MR.strings.edit_tournament))
                    }
                    IconButton(onClick = { /*TODO*/ }, enabled = false) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            stringResource(MR.strings.export_tournament_to_file)
                        )
                    }
                    SettingsInfoMenu(navigator = navigator, showInfoDialog = showInfo)
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = { Icon(Icons.Default.Add, null) },
                text = {
                    Text(
                        stringResource(
                            if (pagerState.currentPage == 0) {
                                MR.strings.new_game
                            } else {
                                MR.strings.add_new_player
                            }
                        )
                    )
                },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                onClick = {
                    if (pagerState.currentPage == 0) {
                        tournament.current = null
                        navigator.push(Screen.GameEditor)
                    } else {
                        showNewPlayerDialog = true
                    }
                },
                modifier = Modifier.animateContentSize(),
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        TournamentViewerContent(
            navigator = navigator,
            tournament = tournament,
            showDeletePlayerDialog = showDeletePlayerDialog,
            playerToBeDeleted = playerToBeDeleted,
            pagerState = pagerState,
            scope = scope,
            modifier = Modifier.padding(paddingValues),
        )
        InfoDialog(showInfo)
        if (showNewPlayerDialog) {
            var newName by rememberSaveable { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewPlayerDialog = false },
                title = { Text(stringResource(MR.strings.add_new_player)) },
                text = {
                    Column {
                        Text(
                            text = stringResource(MR.strings.add_new_player_info),
                            modifier = Modifier.padding(normalPadding)
                        )
                        val invalidName = stringResource(MR.strings.invalid_name)
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { value ->
                                if (value.contains(";")) {
                                    scope.launch { hostState.showSnackbar(invalidName) }
                                } else if (value.length < 100) {
                                    newName = value
                                }
                            },
                            singleLine = true,
                            label = { Text(stringResource(MR.strings.name)) },
                            placeholder = { Text(stringResource(MR.strings.name_unique)) },
                            modifier = Modifier.fillMaxWidth().padding(normalPadding),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showNewPlayerDialog = false
                            val newTournament =
                                TournamentWithGames(tournament.t, tournament.games).apply {
                                    t.players = t.players.toMutableList().apply { add(newName) }
                                    for (game in games) {
                                        game.ranking =
                                            game.ranking.toMutableMap().apply { set(newName, 0) }
                                    }
                                }
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    tournamentDao.upsert(newTournament.t)
                                    gameDao.upsert(*newTournament.games.toTypedArray())
                                }
                            }
                        },
                        enabled = newName.isNotBlank() && !tournament.t.players.contains(newName),
                    ) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({ showNewPlayerDialog = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            )
        }
        if (showDeletePlayerDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeletePlayerDialog.value = false },
                title = {
                    Text("${stringResource(MR.strings.delete_player)} ${playerToBeDeleted.value}?")
                },
                text = { Text(stringResource(MR.strings.delete_player_hint)) },
                confirmButton = {
                    TextButton({
                        showDeletePlayerDialog.value = false
                        val newTournament =
                            TournamentWithGames(tournament.t, tournament.games).apply {
                                t.players = t.players.toMutableList()
                                    .apply { remove(playerToBeDeleted.value) }
                                for (game in games) {
                                    game.ranking = game.ranking.toMutableMap()
                                        .apply { remove(playerToBeDeleted.value) }
                                }
                            }
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                tournamentDao.upsert(newTournament.t)
                                gameDao.upsert(*newTournament.games.toTypedArray())
                            }
                        }
                    }) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({ showDeletePlayerDialog.value = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            )
        }
    }
}
