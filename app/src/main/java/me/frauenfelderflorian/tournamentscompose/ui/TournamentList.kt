package me.frauenfelderflorian.tournamentscompose.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID
import kotlin.reflect.KFunction1
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.GameDao
import me.frauenfelderflorian.tournamentscompose.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.data.TournamentWithGames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(
    navController: NavHostController,
    tournaments: Map<UUID, TournamentWithGames>,
    setCurrent: KFunction1<UUID?, Unit>,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
    intent: Intent,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = rememberSaveable { mutableStateOf(false) }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var showedImport by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val exportToFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(stringResource(R.string.file_mime))
    ) { uri ->
        try {
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(gson.toJson(tournaments.values).toByteArray())
                    it.close()
                }
            } else {
                scope.launch { hostState.showSnackbar(context.getString(R.string.exception_file)) }
            }
        } catch (e: java.lang.Exception) {
            scope.launch {
                hostState.showSnackbar(
                    context.getString(
                        when (e) {
                            is FileNotFoundException -> R.string.exception_file
                            is IOException -> R.string.exception_io
                            else -> R.string.exception
                        }
                    )
                )
            }
        }
    }
    val importFromFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        importFromUri(
            uri = it,
            context = context,
            scope = scope,
            hostState = hostState,
            tournamentDao = tournamentDao,
            gameDao = gameDao,
        )
    }

    LaunchedEffect(Unit) {
        if (!showedImport && intent.data != null && intent.action == Intent.ACTION_VIEW && intent.data!!.scheme == "content") {
            showImport = true
        }
    }

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
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton({ expanded = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.more_actions))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_tournaments_to_file)) },
                                onClick = {
                                    expanded = false
                                    exportToFile.launch(
                                        context.getString(R.string.file_name_tournaments)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_from_file)) },
                                onClick = {
                                    expanded = false
                                    importFromFile.launch(
                                        arrayOf(context.getString(R.string.file_mime))
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                            )
                        }
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
        snackbarHost = { SnackbarHost(hostState) },
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
        InfoDialog(showInfo)
        if (showImport) {
            AlertDialog(
                onDismissRequest = {
                    showImport = false
                    showedImport = true
                },
                icon = { Icon(Icons.Default.ArrowDownward, null) },
                title = { Text(stringResource(R.string.import_)) },
                text = { Text(stringResource(R.string.import_info)) },
                confirmButton = {
                    TextButton({
                        showImport = false
                        importFromUri(
                            uri = intent.data,
                            context = context,
                            scope = scope,
                            hostState = hostState,
                            tournamentDao = tournamentDao,
                            gameDao = gameDao,
                        )
                        showedImport = true
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({
                        showImport = false
                        showedImport = true
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}
