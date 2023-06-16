package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameEditor(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    dao: GameDao,
) {
    var today = System.currentTimeMillis()
    today -= today % 86400000 // Remove the passed milliseconds since the beginning of the day
    var date by rememberSaveable { mutableStateOf(tournament.current?.date ?: today) }
    var hoopsString by rememberSaveable {
        mutableStateOf(
            if (tournament.current == null) "" else tournament.current!!.hoops.toString()
        )
    }
    var hoopReachedString by rememberSaveable {
        mutableStateOf(
            if (tournament.current == null) "" else tournament.current!!.hoopReached.toString()
        )
    }
    var difficulty by rememberSaveable { mutableStateOf(tournament.current?.difficulty ?: "") }
    var selectablePlayers by rememberSaveable {
        mutableStateOf(tournament.t.players.toMutableList().apply {
            if (tournament.current != null) removeAll { tournament.current!!.ranking[it] != 0 }
            add(0, "---")
        }.toList())
    }
    var selectedPlayers by rememberSaveable {
        mutableStateOf(List(tournament.t.players.size) { "---" }.toMutableList().apply {
            if (tournament.current != null) {
                tournament.current!!.playersByRank.forEach {
                    set(tournament.current!!.playersByRank.indexOf(it), it)
                }
            }
        }.toList())
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
                title = { TopAppBarTitle(stringResource(MR.strings.edit_game), scrollBehavior) },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    if (tournament.current != null) {
                        IconButton({ deleteDialogOpen = true }) {
                            Icon(Icons.Default.Delete, stringResource(MR.strings.delete_game))
                        }
                    }
                    val context = LocalContext.current
                    IconButton({
                        try {
                            if (hoopsString.toInt() < 1) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        MR.strings.number_hoops_too_small.getString(context)
                                    )
                                }
                                return@IconButton
                            } else if (hoopReachedString.toInt() < 1) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        MR.strings.number_hoop_reached_too_small.getString(context)
                                    )
                                }
                                return@IconButton
                            } else if (hoopReachedString.toInt() > hoopsString.toInt()) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        MR.strings.number_hoop_reached_too_big.getString(context)
                                    )
                                }
                                return@IconButton
                            }
                            val ranks =
                                selectedPlayers.toMutableList().apply { removeAll { it == "---" } }
                                    .toList()
                            if (ranks.size < 2) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        MR.strings.ranking_invalid.getString(context)
                                    )
                                }
                                return@IconButton
                            }
                            val g = if (tournament.current == null) {
                                Game(
                                    id = UUID.randomUUID(),
                                    tournamentId = tournament.t.id,
                                    date = date,
                                    hoops = hoopsString.toInt(),
                                    hoopReached = hoopReachedString.toInt(),
                                    difficulty = difficulty.trim(),
                                )
                            } else {
                                tournament.current!!.copy(
                                    date = date,
                                    hoops = hoopsString.toInt(),
                                    hoopReached = hoopReachedString.toInt(),
                                    difficulty = difficulty.trim(),
                                )
                            }
                            val ranking = mutableMapOf<String, Int>()
                            ranks.forEach { ranking[it] = ranks.indexOf(it) + 1 }
                            tournament.t.players.toMutableList().apply { removeAll(ranks) }
                                .forEach { ranking[it] = 0 }
                            g.ranking = ranking
                            scope.launch { withContext(Dispatchers.IO) { dao.upsert(g) } }
                            navigator.pop()
                        } catch (e: NumberFormatException) {
                            scope.launch {
                                hostState.showSnackbar(MR.strings.invalid_number.getString(context))
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, stringResource(MR.strings.save_and_exit))
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
        var dateDialogOpen by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues)) {
            val pagerState = rememberPagerState()
            TabRow(pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(MR.strings.details)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(MR.strings.ranking)) },
                )
            }
            HorizontalPager(pageCount = 2, state = pagerState) { page ->
                if (page == 0) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(normalDp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(normalPadding),
                            ) {
                                Icon(Icons.Default.Event, null)
                                OutlinedButton(
                                    onClick = { dateDialogOpen = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = "${stringResource(MR.strings.date)}: ${
                                            formatDate(date)
                                        }",
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(normalDp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(normalPadding),
                            ) {
                                val context = LocalContext.current
                                OutlinedTextField(
                                    value = hoopsString,
                                    onValueChange = {
                                        try {
                                            if (it != "") it.toInt()
                                            hoopsString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch {
                                                hostState.showSnackbar(
                                                    MR.strings.invalid_number.getString(context)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    label = { Text(stringResource(MR.strings.hoops)) },
                                    supportingText = { Text(stringResource(MR.strings.hoops_desc)) },
                                    leadingIcon = { Icon(Icons.Default.Flag, null) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = hoopReachedString,
                                    onValueChange = {
                                        try {
                                            if (it != "") it.toInt()
                                            hoopReachedString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch {
                                                hostState.showSnackbar(
                                                    MR.strings.invalid_number.getString(context)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    label = { Text(stringResource(MR.strings.hoop_reached)) },
                                    supportingText = {
                                        Text(stringResource(MR.strings.hoop_reached_desc))
                                    },
                                    trailingIcon = { Icon(Icons.Default.FlagCircle, null) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = difficulty,
                                onValueChange = { if (it.length < 100) difficulty = it },
                                singleLine = true,
                                label = { Text(stringResource(MR.strings.difficulty)) },
                                placeholder = { Text(stringResource(MR.strings.difficulty_placeholder)) },
                                modifier = Modifier.fillMaxWidth().padding(normalPadding),
                            )
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(tournament.t.players) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.padding(normalPadding),
                            ) {
                                OutlinedTextField(
                                    value = selectedPlayers[tournament.t.players.indexOf(it)],
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.exposedDropdownSize(),
                                ) {
                                    selectablePlayers.forEach { selected ->
                                        DropdownMenuItem(
                                            text = { Text(selected) },
                                            onClick = {
                                                expanded = false
                                                if (selected != "---") {
                                                    selectablePlayers =
                                                        selectablePlayers.toMutableList()
                                                            .apply { remove(selected) }.toList()
                                                }
                                                if (selectedPlayers[tournament.t.players.indexOf(it)] != "---") {
                                                    selectablePlayers =
                                                        selectablePlayers.toMutableList().apply {
                                                            add(
                                                                selectedPlayers[tournament.t.players.indexOf(
                                                                    it
                                                                )]
                                                            )
                                                        }.toList()
                                                }
                                                selectedPlayers =
                                                    selectedPlayers.toMutableList().apply {
                                                        set(
                                                            tournament.t.players.indexOf(it),
                                                            selected,
                                                        )
                                                    }.toList()
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (dateDialogOpen) {
            val datePickerState = rememberDatePickerState(date)
            val confirmEnabled by remember {
                derivedStateOf { datePickerState.selectedDateMillis != null }
            }
            DatePickerDialog(
                onDismissRequest = { dateDialogOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dateDialogOpen = false
                            date = datePickerState.selectedDateMillis!!
                        },
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({ dateDialogOpen = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = { it in tournament.t.start..tournament.t.end },
                )
            }
        }
        if (deleteDialogOpen) {
            AlertDialog(
                onDismissRequest = { deleteDialogOpen = false },
                confirmButton = {
                    TextButton({
                        deleteDialogOpen = false
                        scope.launch {
                            withContext(Dispatchers.IO) { dao.delete(tournament.current!!) }
                        }
                        navigator.popWhile { top: Screen -> top !is Screen.TournamentViewer }
                    }) {
                        Text(stringResource(MR.strings.delete_game))
                    }
                },
                dismissButton = {
                    TextButton({ deleteDialogOpen = false }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
                icon = { Icon(Icons.Default.Delete, null) },
                title = { Text("${stringResource(MR.strings.delete_game)}?") },
                text = { Text(stringResource(MR.strings.delete_game_hint)) },
            )
        }
        InfoDialog(showInfo)
    }
}
