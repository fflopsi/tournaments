package me.frauenfelderflorian.tournamentscompose.ui

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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.GameDao
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.data.TournamentWithGames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(
    navController: NavController,
    tournament: TournamentWithGames?,
    current: UUID?,
    dao: TournamentDao,
    gameDao: GameDao,
    defaultPlayers: List<String>,
    defaultAdaptivePoints: Boolean,
    defaultFirstPoints: Int,
) {
    var name by rememberSaveable { mutableStateOf(tournament?.t?.name ?: "") }
    var today = System.currentTimeMillis()
    today -= today % 86400000 // Remove the passed milliseconds since the beginning of the day
    var start by rememberSaveable { mutableStateOf(tournament?.t?.start ?: today) }
    var end by rememberSaveable { mutableStateOf(tournament?.t?.end ?: (today + 7 * 86400000)) }
    var useDefaults by rememberSaveable { mutableStateOf(false) }
    val players = rememberMutableStateListOf<String>()
    // ugly, but LaunchedEffect does not seem to work
    if (players.joinToString("").trim() == "") players.clear()
    val adaptivePoints = rememberSaveable {
        mutableStateOf(tournament?.t?.useAdaptivePoints ?: true)
    }
    val firstPoints = rememberSaveable { mutableStateOf(tournament?.t?.firstPoints) }

    LaunchedEffect(Unit) {
        val newPlayers =
            navController.currentBackStackEntry?.savedStateHandle?.get<String>("players")
        if (newPlayers != null) {
            players.clear()
            newPlayers.split(";").forEach { players.add(it) }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("players")
        }
    }

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(stringResource(R.string.edit_tournament), scrollBehavior)
                },
                navigationIcon = { BackButton(navController) },
                actions = {
                    if (current != null) {
                        IconButton({ deleteDialogOpen = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete_tournament))
                        }
                    }
                    val context = LocalContext.current
                    IconButton({
                        val t: Tournament
                        if (current == null) {
                            if (!useDefaults && players.size < 2 || useDefaults && defaultPlayers.size < 2) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(R.string.at_least_two_players)
                                    )
                                }
                                return@IconButton
                            }
                            if (useDefaults) {
                                t = Tournament(
                                    id = UUID.randomUUID(),
                                    name = name.trim(),
                                    start = start,
                                    end = end,
                                    useAdaptivePoints = defaultAdaptivePoints,
                                    firstPoints = defaultFirstPoints,
                                ).apply { this.players = defaultPlayers }
                            } else if (adaptivePoints.value) {
                                t = Tournament(
                                    id = UUID.randomUUID(),
                                    name = name.trim(),
                                    start = start,
                                    end = end,
                                    useAdaptivePoints = true,
                                ).apply { this.players = players }
                            } else if (firstPoints.value != null) {
                                t = Tournament(
                                    id = UUID.randomUUID(),
                                    name = name.trim(),
                                    start = start,
                                    end = end,
                                    useAdaptivePoints = false,
                                    firstPoints = firstPoints.value!!.toInt(),
                                ).apply { this.players = players }
                            } else {
                                scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(
                                            R.string.enter_number_first_points
                                        )
                                    )
                                }
                                return@IconButton
                            }
                        } else {
                            if (adaptivePoints.value) {
                                t = tournament!!.t.copy(
                                    name = name.trim(),
                                    start = start,
                                    end = end,
                                    useAdaptivePoints = true
                                )
                            } else if (firstPoints.value != null) {
                                t = tournament!!.t.copy(
                                    name = name.trim(),
                                    start = start,
                                    end = end,
                                    useAdaptivePoints = false,
                                    firstPoints = firstPoints.value!!.toInt()
                                )
                            } else {
                                scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(
                                            R.string.enter_number_first_points
                                        )
                                    )
                                }
                                return@IconButton
                            }
                        }
                        scope.launch { withContext(Dispatchers.IO) { dao.upsert(t) } }
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                    }
                    SettingsInfoMenu(navController = navController, showInfoDialog = showInfo)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            TextField(
                value = name,
                onValueChange = { if (it.length < 100) name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.name)) },
                placeholder = { Text(stringResource(R.string.give_meaningful_name)) },
                trailingIcon = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(normalPadding),
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
                    Text("${stringResource(R.string.start_date)}: ${formatDate(start)}")
                }
                OutlinedButton(
                    onClick = { endDialogOpen = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("${stringResource(R.string.end_date)}: ${formatDate(end)}")
                }
            }
            if (current == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { useDefaults = !useDefaults }
                        .padding(normalPadding),
                ) {
                    Column(Modifier.weight(2f)) {
                        Text(text = stringResource(R.string.use_defaults), style = titleStyle)
                        Text(
                            text = stringResource(R.string.use_defaults_desc),
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
                    PlayersSetting(navController = navController, players = players)
                }
            }
            AnimatedVisibility(
                visible = current != null || !useDefaults,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                val context = LocalContext.current
                PointSystemSettings(
                    adaptivePoints = adaptivePoints,
                    onClickAdaptivePoints = { adaptivePoints.value = !adaptivePoints.value },
                    firstPoints = firstPoints,
                    onChangeFirstPoints = {
                        try {
                            if (it != "") it.toInt()
                            firstPoints.value = it.toIntOrNull()
                        } catch (e: NumberFormatException) {
                            scope.launch {
                                hostState.showSnackbar(
                                    context.resources.getString(R.string.invalid_number)
                                )
                            }
                        }
                    },
                )
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ startDialogOpen = false }) {
                        Text(stringResource(R.string.cancel))
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ endDialogOpen = false }) {
                        Text(stringResource(R.string.cancel))
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
                        navController.popBackStack(Routes.TOURNAMENT_LIST.route, false)
                    }) {
                        Text(stringResource(R.string.delete_tournament))
                    }
                },
                dismissButton = {
                    TextButton({ deleteDialogOpen = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                icon = { Icon(Icons.Default.Delete, null) },
                title = { Text("${stringResource(R.string.delete_tournament)}?") },
                text = { Text(stringResource(R.string.delete_tournament_hint)) },
            )
        }
        InfoDialog(showInfo)
    }
}
