package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.Tournament
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun TournamentEditor(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames?,
    current: UUID?,
    setCurrent: (UUID) -> Unit,
    tournaments: Map<UUID, TournamentWithGames>,
    dao: TournamentDao,
    gameDao: GameDao,
    prefs: Prefs,
    playersModel: PlayersModel,
) {
    var name by rememberSaveable { mutableStateOf(tournament?.t?.name ?: "") }
    var today = System.currentTimeMillis()
    today -= today % 86400000 // Remove the passed milliseconds since the beginning of the day
    var start by rememberSaveable { mutableLongStateOf(tournament?.t?.start ?: today) }
    var end by rememberSaveable { mutableLongStateOf(tournament?.t?.end ?: (today + 7 * 86400000)) }
    var useDefaults by rememberSaveable { mutableStateOf(false) }
    val players = rememberMutableStateListOf<String>()
    var adaptivePoints by rememberSaveable {
        mutableStateOf(tournament?.t?.useAdaptivePoints ?: true)
    }
    var firstPoints by rememberSaveable { mutableStateOf(tournament?.t?.firstPoints) }
    var uuid: UUID? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(playersModel.edited) {
        if (playersModel.edited) {
            players.clear()
            players.addAll(playersModel.players)
            playersModel.edited = false
        }
    }

    LaunchedEffect(tournaments[uuid]) {
        if (tournaments[uuid] != null) {
            setCurrent(uuid!!)
            navigator.pop()
            navigator.push(Screen.TournamentViewer)
        }
    }

    val atLeastTwoPlayers = stringResource(Res.string.at_least_two_players)
    val enterNumberFirstPoints = stringResource(Res.string.enter_number_first_points)
    fun save() {
        val t: Tournament
        if (current == null) {
            if (!useDefaults && players.size < 2 || useDefaults && prefs.players.size < 2) {
                scope.launch { hostState.showSnackbar(atLeastTwoPlayers) }
                return
            }
            if (useDefaults) {
                t = Tournament(
                    id = UUID.randomUUID(),
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = prefs.adaptivePoints,
                    firstPoints = prefs.firstPoints,
                ).apply { this.players = prefs.players }
            } else if (adaptivePoints) {
                t = Tournament(
                    id = UUID.randomUUID(),
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = true,
                ).apply { this.players = players }
            } else if (firstPoints != null) {
                t = Tournament(
                    id = UUID.randomUUID(),
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = false,
                    firstPoints = firstPoints!!.toInt(),
                ).apply { this.players = players }
            } else {
                scope.launch { hostState.showSnackbar(enterNumberFirstPoints) }
                return
            }
            uuid = t.id
        } else {
            if (adaptivePoints) {
                t = Tournament(
                    id = tournament!!.t.id,
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = true,
                ).apply { this.players = tournament.t.players }
            } else if (firstPoints != null) {
                t = Tournament(
                    id = tournament!!.t.id,
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = false,
                    firstPoints = firstPoints!!.toInt(),
                ).apply { this.players = tournament.t.players }
            } else {
                scope.launch { hostState.showSnackbar(enterNumberFirstPoints) }
                return
            }
            navigator.pop()
        }
        scope.launch { withContext(Dispatchers.IO) { dao.upsert(t) } }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(stringResource(Res.string.edit_tournament), scrollBehavior)
                },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    if (current != null) {
                        IconButton({ deleteDialogOpen = true }) {
                            Icon(Icons.Default.Delete, stringResource(Res.string.delete_tournament))
                        }
                    }
                    IconButton(::save) {
                        Icon(Icons.Default.Check, stringResource(Res.string.save_and_exit))
                    }
                    SettingsInfoMenu(navigator = navigator, showInfoDialog = showInfo)
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        var datesDialogOpen by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier.weight(2f).verticalScroll(rememberScrollState()),
            ) {
                TextField(
                    value = name,
                    onValueChange = { if (it.length < 100) name = it },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.name)) },
                    placeholder = { Text(stringResource(Res.string.give_meaningful_name)) },
                    trailingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth().padding(normalPadding),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(normalPadding),
                ) {
                    OutlinedButton(
                        onClick = { datesDialogOpen = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("${stringResource(Res.string.start_date)}: ${formatDate(start)}; ${stringResource(Res.string.end_date)}: ${formatDate(end)}")
                    }
                }
                if (current == null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(normalDp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { useDefaults = !useDefaults }
                            .padding(normalPadding),
                    ) {
                        Column(Modifier.weight(2f)) {
                            Text(text = stringResource(Res.string.use_defaults), style = titleStyle)
                            Text(
                                text = stringResource(Res.string.use_defaults_desc),
                                style = detailsStyle,
                            )
                        }
                        Switch(checked = useDefaults, onCheckedChange = null)
                    }
                    AnimatedVisibility(
                        visible = !useDefaults,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        val player = stringResource(Res.string.player)
                        PlayersSetting(players) {
                            playersModel.players.clear()
                            playersModel.players.addAll(players.ifEmpty {
                                listOf("$player 1", "$player 2")
                            })
                            navigator.push(Screen.PlayersEditor)
                        }
                    }
                }
                AnimatedVisibility(
                    visible = current != null || !useDefaults,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    val invalidNumber = stringResource(Res.string.invalid_number)
                    PointSystemSettings(
                        adaptivePoints = adaptivePoints,
                        onClickAdaptivePoints = { adaptivePoints = !adaptivePoints },
                        firstPoints = firstPoints,
                        onChangeFirstPoints = {
                            try {
                                if (it != "") it.toInt()
                                firstPoints = it.toIntOrNull()
                            } catch (e: NumberFormatException) {
                                scope.launch { hostState.showSnackbar(invalidNumber) }
                            }
                        },
                    )
                }
            }
            if (prefs.experimentalFeatures && current == null) {
                Button(
                    onClick = ::save,
                    modifier = Modifier.padding(normalPadding).fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.create_tournament))
                }
            }
        }
        if (datesDialogOpen) {
            val dateRangePickerState = rememberDateRangePickerState(
                initialSelectedStartDateMillis = start,
                initialSelectedEndDateMillis = end,
            )
            val datesDialogSheetState = rememberModalBottomSheetState(true)
            val confirmEnabled by remember {
                derivedStateOf {
                    dateRangePickerState.selectedStartDateMillis != null
                            && dateRangePickerState.selectedEndDateMillis != null
                }
            }
            ModalBottomSheet(
                onDismissRequest = { datesDialogOpen = false },
                sheetState = datesDialogSheetState,
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(normalPadding),
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch { datesDialogSheetState.hide() }.invokeOnCompletion {
                                    if (!datesDialogSheetState.isVisible) {
                                        datesDialogOpen = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, stringResource(Res.string.cancel))
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch { datesDialogSheetState.hide() }.invokeOnCompletion {
                                    if (!datesDialogSheetState.isVisible) {
                                        datesDialogOpen = false
                                    }
                                }
                                start = dateRangePickerState.selectedStartDateMillis!!
                                end = dateRangePickerState.selectedEndDateMillis!!
                            },
                            enabled = confirmEnabled,
                        ) {
                            Text(stringResource(Res.string.save_and_exit))
                        }
                    }
                    DateRangePicker(dateRangePickerState)
                }
            }
        }
        if (deleteDialogOpen) {
            AlertDialog(
                onDismissRequest = { deleteDialogOpen = false },
                confirmButton = {
                    TextButton({
                        deleteDialogOpen = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                dao.delete(tournament!!.t)
                                tournament.games.forEach { gameDao.delete(it) }
                            }
                        }
                        navigator.popTo(0)
                    }) {
                        Text(stringResource(Res.string.delete_tournament))
                    }
                },
                dismissButton = {
                    TextButton({ deleteDialogOpen = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
                icon = { Icon(Icons.Default.Delete, null) },
                title = { Text("${stringResource(Res.string.delete_tournament)}?") },
                text = { Text(stringResource(Res.string.delete_tournament_hint)) },
            )
        }
        InfoDialog(showInfo)
    }
}
