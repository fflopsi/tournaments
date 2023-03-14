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
    tournament: Tournament,
    players: List<String>,
    games: MutableList<Game>,
    current: Int
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var dateDialogOpen by remember { mutableStateOf(false) }

    var date by rememberSaveable { mutableStateOf(if (current == -1) System.currentTimeMillis() else games[current].date) }
    var hoopsString by rememberSaveable { mutableStateOf(if (current == -1) "" else games[current].hoops.toString()) }
    var hoopReachedString by rememberSaveable { mutableStateOf(if (current == -1) "" else games[current].hoopReached.toString()) }
    var difficulty by rememberSaveable { mutableStateOf(if (current == -1) "" else games[current].difficulty) }
    var selectablePlayers by rememberSaveable {
        mutableStateOf(players.toMutableList().apply {
            if (current != -1) removeAll { games[current].ranking[it] != 0 }
            add(0, "---")
        }.toList())
    }
    var selectedPlayers by rememberSaveable {
        mutableStateOf(List(players.size) { "---" }.toMutableList().apply {
            if (current != -1)
                games[current].playersByRank.forEach {
                    set(games[current].playersByRank.indexOf(it), it)
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
                        if (current != -1)
                            IconButton(onClick = {
                                games.remove(games[current])
                                navController.popBackStack()
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
                                val absent =
                                    players.toMutableList().apply { removeAll(ranks) }.toList()
                                if (current == -1)
                                    games.add(
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
                                    games[current].apply {
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                                            if (it != "")
                                                try {
                                                    it.toInt()
                                                    hoopsString = it.trim()
                                                } catch (e: NumberFormatException) {
                                                    scope.launch {
                                                        hostState.showSnackbar(
                                                            context.resources.getString(R.string.no_invalid_integer)
                                                        )
                                                    }
                                                }
                                            else hoopsString = ""
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
                                            if (it != "") //TODO: copy this if/else where needed
                                                try {
                                                    it.toInt()
                                                    hoopReachedString = it.trim()
                                                } catch (e: NumberFormatException) {
                                                    scope.launch {
                                                        hostState.showSnackbar(
                                                            context.resources.getString(R.string.no_invalid_integer)
                                                        )
                                                    }
                                                }
                                            else hoopReachedString = ""
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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(items = players) {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedPlayers[players.indexOf(it)],
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded
                                            )
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
                                                    if (selectedPlayers[players.indexOf(it)] != "---")
                                                        selectablePlayers =
                                                            selectablePlayers.toMutableList()
                                                                .apply {
                                                                    add(
                                                                        selectedPlayers[
                                                                                players.indexOf(it)
                                                                        ]
                                                                    )
                                                                }.toList()
                                                    selectedPlayers =
                                                        selectedPlayers.toMutableList().apply {
                                                            set(players.indexOf(it), selected)
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