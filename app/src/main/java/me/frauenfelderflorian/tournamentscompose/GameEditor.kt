package me.frauenfelderflorian.tournamentscompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.data.Game
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameEditor(
    navController: NavController,
    theme: Int,
    tournament: Tournament
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var dateDialogOpen by remember { mutableStateOf(false) }

    var date by rememberSaveable { mutableStateOf(if (tournament.current == -1) System.currentTimeMillis() else tournament.games[tournament.current].date) }
    var hoopsString by rememberSaveable { mutableStateOf(if (tournament.current == -1) "" else tournament.games[tournament.current].hoops.toString()) }
    var hoopReachedString by rememberSaveable { mutableStateOf(if (tournament.current == -1) "" else tournament.games[tournament.current].hoopReached.toString()) }
    var difficulty by rememberSaveable { mutableStateOf(if (tournament.current == -1) "" else tournament.games[tournament.current].difficulty) }
    var selectablePlayers by rememberSaveable {
        mutableStateOf(tournament.players.toMutableList().apply {
            if (tournament.current != -1) removeAll { tournament.games[tournament.current].ranking[it] != 0 }
            add(0, "---")
        }.toList())
    }
    var selectedPlayers by rememberSaveable {
        mutableStateOf(List(tournament.players.size) { "---" }.toMutableList().apply {
            if (tournament.current != -1)
                tournament.games[tournament.current].playersByRank.forEach {
                    set(tournament.games[tournament.current].playersByRank.indexOf(it), it)
                }
        }.toList())
    }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = { Text(stringResource(R.string.edit_game)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (tournament.current != -1)
                            IconButton(onClick = {
                                tournament.games.remove(tournament.games[tournament.current])
                                navController.popBackStack(Routes.TOURNAMENT_VIEWER.route, false)
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.delete_game))
                            }
                        val context = LocalContext.current
                        IconButton(onClick = {
                            try {
                                if (hoopsString.toInt() < 1) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(R.string.number_hoops_too_small)
                                        )
                                    }
                                    return@IconButton
                                } else if (hoopReachedString.toInt() < 1) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(R.string.number_hoops_reached_too_small)
                                        )
                                    }
                                    return@IconButton
                                } else if (hoopReachedString.toInt() > hoopsString.toInt()) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(R.string.number_hoops_reached_too_big)
                                        )
                                    }
                                    return@IconButton
                                }
                                val ranks = selectedPlayers.toMutableList()
                                    .apply { removeAll { it == "---" } }.toList()
                                if (ranks.size < 2) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(R.string.ranking_invalid)
                                        )
                                    }
                                    return@IconButton
                                }
                                val absent = tournament.players.toMutableList()
                                    .apply { removeAll(ranks) }.toList()
                                if (tournament.current == -1)
                                    tournament.games.add(
                                        Game(
                                            date = date,
                                            hoops = hoopsString.toInt(),
                                            hoopReached = hoopReachedString.toInt()
                                        ).apply {
                                            this.difficulty = difficulty
                                            ranks.forEach {
                                                this.ranking[it] = ranks.indexOf(it) + 1
                                            }
                                            absent.forEach { this.ranking[it] = 0 }
                                        }
                                    )
                                else
                                    tournament.games[tournament.current].apply {
                                        this.date = date
                                        hoops = hoopsString.toInt()
                                        hoopReached = hoopReachedString.toInt()
                                        this.difficulty = difficulty
                                        ranks.forEach {
                                            this.ranking[it] = ranks.indexOf(it) + 1
                                        }
                                        absent.forEach { this.ranking[it] = 0 }
                                    }
                                navController.popBackStack()
                            } catch (e: NumberFormatException) {
                                scope.launch {
                                    hostState.showSnackbar(
                                        context.resources.getString(R.string.no_invalid_integer)
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(hostState = hostState) },
            contentWindowInsets = WindowInsets.ime,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.details)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.ranking)) }
                    )
                }
                val columnScope = this
                Box {
                    columnScope.AnimatedVisibility(
                        visible = selectedTab == 0,
                        enter = slideInHorizontally(initialOffsetX = { width -> -width }),
                        exit = slideOutHorizontally(targetOffsetX = { width -> -width })
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Event, null)
                                    OutlinedButton(
                                        onClick = { dateDialogOpen = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            stringResource(R.string.date) + ": " + formatDate(date),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                                        context.resources.getString(R.string.no_invalid_integer)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        label = { Text(stringResource(R.string.hoops)) },
                                        supportingText = { Text(stringResource(R.string.hoops_desc)) },
                                        leadingIcon = { Icon(Icons.Default.Flag, null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
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
                                                        context.resources.getString(R.string.no_invalid_integer)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        label = { Text(stringResource(R.string.hoop_reached)) },
                                        supportingText = { Text(stringResource(R.string.hoop_reached_desc)) },
                                        trailingIcon = { Icon(Icons.Default.FlagCircle, null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = difficulty,
                                    onValueChange = { difficulty = it },
                                    singleLine = true,
                                    label = { Text(stringResource(R.string.difficulty)) },
                                    placeholder = { Text(stringResource(R.string.difficulty_placeholder)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    columnScope.AnimatedVisibility(
                        visible = selectedTab == 1,
                        enter = slideInHorizontally(initialOffsetX = { width -> width }),
                        exit = slideOutHorizontally(targetOffsetX = { width -> width })
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(items = tournament.players) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedPlayers[tournament.players.indexOf(it)],
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.exposedDropdownSize() //TODO: remove workaround
                                    ) {
                                        selectablePlayers.forEach { selected ->
                                            DropdownMenuItem(
                                                text = { Text(selected) },
                                                onClick = {
                                                    expanded = false
                                                    if (selected != "---")
                                                        selectablePlayers =
                                                            selectablePlayers.toMutableList()
                                                                .apply { remove(selected) }.toList()
                                                    if (selectedPlayers[tournament.players
                                                            .indexOf(it)] != "---"
                                                    )
                                                        selectablePlayers =
                                                            selectablePlayers.toMutableList()
                                                                .apply {
                                                                    add(
                                                                        selectedPlayers[tournament
                                                                            .players.indexOf(it)]
                                                                    )
                                                                }.toList()
                                                    selectedPlayers =
                                                        selectedPlayers.toMutableList().apply {
                                                            set(
                                                                tournament.players.indexOf(it),
                                                                selected
                                                            )
                                                        }.toList()
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
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
                            enabled = confirmEnabled
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dateDialogOpen = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        dateValidator = { it in tournament.start..tournament.end }
                    )
                }
            }
        }
    }
}
