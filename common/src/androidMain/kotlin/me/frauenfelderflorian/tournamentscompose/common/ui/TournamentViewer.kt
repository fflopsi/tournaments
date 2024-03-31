package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.*

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalResourceApi::class
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
    val pagerState =
        rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f, pageCount = { 2 })
    val showInfo = remember { mutableStateOf(false) }
    var showNewPlayerDialog by remember { mutableStateOf(false) }
    val showDeletePlayerDialog = remember { mutableStateOf(false) }
    val playerToBeDeleted = remember { mutableStateOf("") }
    val showRenamePlayerDialog = remember { mutableStateOf(false) }
    val playerToBeRenamed = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val exportToFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(stringResource(Res.string.file_mime))
    ) {
        scope.launch {
            exportToUri(
                uri = it,
                context = context,
                content = setOf(tournament),
            )
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(Res.string.tournament_title, tournament.t.name),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    IconButton({ navigator.push(Screen.TournamentEditor) }) {
                        Icon(Icons.Default.Edit, stringResource(Res.string.edit_tournament))
                    }
                    val fileEndingTournament = stringResource(Res.string.file_ending_tournament)
                    IconButton({
                        exportToFile.launch("${tournament.t.name}${fileEndingTournament}")
                    }) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            stringResource(Res.string.export_tournament_to_file)
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
                                Res.string.new_game
                            } else {
                                Res.string.add_new_player
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
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        TournamentViewerContent(
            navigator = navigator,
            tournament = tournament,
            showDeletePlayerDialog = showDeletePlayerDialog,
            playerToBeDeleted = playerToBeDeleted,
            showRenamePlayerDialog = showRenamePlayerDialog,
            playerToBeRenamed = playerToBeRenamed,
            pagerState = pagerState,
            scope = scope,
            modifier = Modifier.padding(paddingValues),
        )
        InfoDialog(showInfo)
        if (showNewPlayerDialog) {
            var newName by rememberSaveable { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showNewPlayerDialog = false },
                icon = { Icon(Icons.Default.Add, null) },
                title = { Text(stringResource(Res.string.add_new_player)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(normalDp)) {
                        Text(stringResource(Res.string.add_new_player_info))
                        val invalidName = stringResource(Res.string.invalid_name)
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
                            label = { Text(stringResource(Res.string.name)) },
                            placeholder = { Text(stringResource(Res.string.name_unique)) },
                            modifier = Modifier.fillMaxWidth(),
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
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ showNewPlayerDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
        if (showRenamePlayerDialog.value) {
            var newName by rememberSaveable { mutableStateOf(playerToBeRenamed.value) }
            AlertDialog(
                onDismissRequest = { showRenamePlayerDialog.value = false },
                icon = { Icon(Icons.Default.Edit, null) },
                title = { Text("${stringResource(Res.string.rename_player)} ${playerToBeRenamed.value}") },
                text = {
                    val invalidName = stringResource(Res.string.invalid_name)
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
                        label = { Text(stringResource(Res.string.name)) },
                        placeholder = { Text(stringResource(Res.string.name_unique)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRenamePlayerDialog.value = false
                            val newTournament =
                                TournamentWithGames(tournament.t, tournament.games).apply {
                                    t.players = t.players.toMutableList()
                                        .apply { set(indexOf(playerToBeRenamed.value), newName) }
                                    for (game in games) {
                                        game.ranking = game.ranking.toMutableMap().apply {
                                            set(newName, get(playerToBeRenamed.value)!!)
                                            remove(playerToBeRenamed.value)
                                        }
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
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ showRenamePlayerDialog.value = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
        if (showDeletePlayerDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeletePlayerDialog.value = false },
                icon = { Icon(Icons.Default.Delete, null) },
                title = {
                    Text("${stringResource(Res.string.delete_player)} ${playerToBeDeleted.value}?")
                },
                text = { Text(stringResource(Res.string.delete_player_hint)) },
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
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ showDeletePlayerDialog.value = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
    }
}
