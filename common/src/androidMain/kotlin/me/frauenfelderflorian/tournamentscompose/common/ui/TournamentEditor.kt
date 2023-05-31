package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.navigation.NavController
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.Tournament
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(
    navigator: Navigator,
    tournament: TournamentWithGames?,
    current: UUID?,
    setCurrent: (UUID) -> Unit,
    tournaments: Map<UUID, TournamentWithGames>,
    dao: TournamentDao,
    gameDao: GameDao,
    prefs: Prefs,
) {
    var name by rememberSaveable { mutableStateOf(tournament?.t?.name ?: "") }
    var today = System.currentTimeMillis()
    today -= today % 86400000 // Remove the passed milliseconds since the beginning of the day
    var start by rememberSaveable { mutableStateOf(tournament?.t?.start ?: today) }
    var end by rememberSaveable { mutableStateOf(tournament?.t?.end ?: (today + 7 * 86400000)) }
    var useDefaults by rememberSaveable { mutableStateOf(false) }
    val players = rememberMutableStateListOf<String>()
    var adaptivePoints by rememberSaveable {
        mutableStateOf(tournament?.t?.useAdaptivePoints ?: true)
    }
    var firstPoints by rememberSaveable { mutableStateOf(tournament?.t?.firstPoints) }
    var uuid: UUID? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stateHandle = (navigator.controller as NavController).currentBackStackEntry?.savedStateHandle
        if (stateHandle?.get<Array<String>>(MR.strings.players_key.getString(context)) != null) {
            players.clear()
            stateHandle.get<Array<String>>(MR.strings.players_key.getString(context))!!
                .forEach { players.add(it) }
            stateHandle.remove<Array<String>>(MR.strings.players_key.getString(context))
        }
    }

    LaunchedEffect(tournaments[uuid]) {
        if (tournaments[uuid] != null) {
            setCurrent(uuid!!)
            navigator.navigate(Routes.TOURNAMENT_VIEWER)
            (navigator.controller as NavController).backQueue.remove(navigator.controller.previousBackStackEntry)
        }
    }

    fun save() {
        val t: Tournament
        if (current == null) {
            if (!useDefaults && players.size < 2 || useDefaults && prefs.players.size < 2) {
                scope.launch {
                    hostState.showSnackbar(MR.strings.at_least_two_players.getString(context))
                }
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
                scope.launch {
                    hostState.showSnackbar(MR.strings.enter_number_first_points.getString(context))
                }
                return
            }
        } else {
            if (adaptivePoints) {
                t = tournament!!.t.copy(
                    name = name.trim(), start = start, end = end, useAdaptivePoints = true
                )
            } else if (firstPoints != null) {
                t = tournament!!.t.copy(
                    name = name.trim(),
                    start = start,
                    end = end,
                    useAdaptivePoints = false,
                    firstPoints = firstPoints!!.toInt()
                )
            } else {
                scope.launch {
                    hostState.showSnackbar(MR.strings.enter_number_first_points.getString(context))
                }
                return
            }
        }
        scope.launch { withContext(Dispatchers.IO) { dao.upsert(t) } }
        if ((navigator.controller as NavController).previousBackStackEntry?.destination?.route == Routes.TOURNAMENT_VIEWER.route) {
            navigator.navigateUp()
        } else {
            uuid = t.id
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(stringResource(MR.strings.edit_tournament), scrollBehavior)
                },
                navigationIcon = { BackButton { navigator.navigateUp() } },
                actions = {
                    if (current != null) {
                        IconButton({ deleteDialogOpen = true }) {
                            Icon(Icons.Default.Delete, stringResource(MR.strings.delete_tournament))
                        }
                    }
                    IconButton(::save) {
                        Icon(Icons.Default.Check, stringResource(MR.strings.save_and_exit))
                    }
                    SettingsInfoMenu(
                        navigateToSettings = {
                            navigator.navigate(Routes.SETTINGS_EDITOR)
                        },
                        showInfoDialog = showInfo,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        var startDialogOpen by remember { mutableStateOf(false) }
        var endDialogOpen by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier.weight(2f).verticalScroll(rememberScrollState()),
            ) {
                TextField(
                    value = name,
                    onValueChange = { if (it.length < 100) name = it },
                    singleLine = true,
                    label = { Text(stringResource(MR.strings.name)) },
                    placeholder = { Text(stringResource(MR.strings.give_meaningful_name)) },
                    trailingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth().padding(normalPadding),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(normalPadding),
                ) {
                    OutlinedButton(
                        onClick = { startDialogOpen = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("${stringResource(MR.strings.start_date)}: ${formatDate(start)}")
                    }
                    OutlinedButton(
                        onClick = { endDialogOpen = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("${stringResource(MR.strings.end_date)}: ${formatDate(end)}")
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
                            Text(text = stringResource(MR.strings.use_defaults), style = titleStyle)
                            Text(
                                text = stringResource(MR.strings.use_defaults_desc),
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
                        PlayersSetting(players) {
                            (navigator.controller as NavController).currentBackStackEntry?.savedStateHandle?.set(
                                MR.strings.players_key.getString(context),
                                if (players.isNotEmpty()) {
                                    players.toTypedArray()
                                } else {
                                    arrayOf(
                                        "${MR.strings.player.getString(context)} 1",
                                        "${MR.strings.player.getString(context)} 2",
                                    )
                                },
                            )
                            navigator.navigate(Routes.PLAYERS_EDITOR)
                        }
                    }
                }
                AnimatedVisibility(
                    visible = current != null || !useDefaults,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    PointSystemSettings(
                        adaptivePoints = adaptivePoints,
                        onClickAdaptivePoints = { adaptivePoints = !adaptivePoints },
                        firstPoints = firstPoints,
                        onChangeFirstPoints = {
                            try {
                                if (it != "") it.toInt()
                                firstPoints = it.toIntOrNull()
                            } catch (e: NumberFormatException) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        MR.strings.invalid_number.getString(context)
                                    )
                                }
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
                    Text(stringResource(MR.strings.create_tournament))
                }
            }
        }
        if (startDialogOpen) {
            val datePickerState = rememberDatePickerState(start)
            val confirmEnabled by remember {
                derivedStateOf { datePickerState.selectedDateMillis != null }
            }
            DatePickerDialog(
                onDismissRequest = { startDialogOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDialogOpen = false
                            start = datePickerState.selectedDateMillis!!
                        },
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({ startDialogOpen = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState, dateValidator = { it < end })
            }
        }
        if (endDialogOpen) {
            val datePickerState = rememberDatePickerState(end)
            val confirmEnabled by remember {
                derivedStateOf { datePickerState.selectedDateMillis != null }
            }
            DatePickerDialog(
                onDismissRequest = { endDialogOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDialogOpen = false
                            end = datePickerState.selectedDateMillis!!
                        },
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({ endDialogOpen = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState, dateValidator = { it > start })
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
                        (navigator.controller as NavController).popBackStack(Routes.TOURNAMENT_LIST.route, false)
                    }) {
                        Text(stringResource(MR.strings.delete_tournament))
                    }
                },
                dismissButton = {
                    TextButton({ deleteDialogOpen = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
                icon = { Icon(Icons.Default.Delete, null) },
                title = { Text("${stringResource(MR.strings.delete_tournament)}?") },
                text = { Text(stringResource(MR.strings.delete_tournament_hint)) },
            )
        }
        InfoDialog(showInfo)
    }
}
