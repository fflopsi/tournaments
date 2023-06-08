package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
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
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(
    navigator: StackNavigation<Screen>,
    tournaments: Map<UUID, TournamentWithGames>,
    setCurrent: (UUID?) -> Unit,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val exportToFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(stringResource(MR.strings.file_mime))
    ) {
        exportToUri(
            uri = it,
            context = context,
            content = tournaments.values,
        )
    }
    val importFromFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        importFromUri(
            uri = it,
            context = context,
            scope = scope,
            tournamentDao = tournamentDao,
            gameDao = gameDao,
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(MR.strings.app_title), scrollBehavior) },
                actions = {
                    IconButton({ navigator.push(Screen.AppSettings) }) {
                        Icon(Icons.Default.Settings, stringResource(MR.strings.settings))
                    }
                    IconButton({ showInfo.value = true }) {
                        Icon(Icons.Outlined.Info, stringResource(MR.strings.about))
                    }
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton({ expanded = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(MR.strings.more_actions))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(MR.strings.export_tournaments_to_file))
                                },
                                onClick = {
                                    expanded = false
                                    exportToFile.launch(
                                        MR.strings.file_name_tournaments.getString(context)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(MR.strings.import_from_file)) },
                                onClick = {
                                    expanded = false
                                    importFromFile.launch(
                                        arrayOf(MR.strings.file_mime.getString(context))
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
                text = { Text(stringResource(MR.strings.new_tournament)) },
                expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                onClick = {
                    setCurrent(null)
                    navigator.push(Screen.TournamentEditor)
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        TournamentListContent(
            tournaments = tournaments,
            setCurrent = setCurrent,
            navigator = navigator,
            modifier = Modifier.padding(paddingValues),
        )
        InfoDialog(showInfo)
    }
}
