package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.data.players
import me.frauenfelderflorian.tournamentscompose.common.data.playersByRank
import me.frauenfelderflorian.tournamentscompose.common.data.ranking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.Res

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalResourceApi::class
)
@Composable
fun GameEditor(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    dao: GameDao,
) {
    var today = System.currentTimeMillis()
    today -= today % 86400000 // Remove the passed milliseconds since the beginning of the day
    var date by rememberSaveable { mutableLongStateOf(tournament.current?.date ?: today) }
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
    val invalidNumber = stringResource(Res.string.invalid_number)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(Res.string.edit_game), scrollBehavior) },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    if (tournament.current != null) {
                        IconButton({ deleteDialogOpen = true }) {
                            Icon(Icons.Default.Delete, stringResource(Res.string.delete_game))
                        }
                    }
                    val numberHoopsTooSmall = stringResource(Res.string.number_hoops_too_small)
                    val numberHoopReachedTooSmall =
                        stringResource(Res.string.number_hoop_reached_too_small)
                    val numberHoopReachedTooBig =
                        stringResource(Res.string.number_hoop_reached_too_big)
                    val rankingInvalid = stringResource(Res.string.ranking_invalid)
                    IconButton({
                        try {
                            if (hoopsString.toInt() < 1) {
                                scope.launch { hostState.showSnackbar(numberHoopsTooSmall) }
                                return@IconButton
                            } else if (hoopReachedString.toInt() < 1) {
                                scope.launch { hostState.showSnackbar(numberHoopReachedTooSmall) }
                                return@IconButton
                            } else if (hoopReachedString.toInt() > hoopsString.toInt()) {
                                scope.launch { hostState.showSnackbar(numberHoopReachedTooBig) }
                                return@IconButton
                            }
                            val ranks =
                                selectedPlayers.toMutableList().apply { removeAll { it == "---" } }
                                    .toList()
                            if (ranks.size < 2) {
                                scope.launch { hostState.showSnackbar(rankingInvalid) }
                                return@IconButton
                            }
                            val g = Game(
                                id = if (tournament.current == null) {
                                    UUID.randomUUID()
                                } else {
                                    tournament.current!!.id
                                },
                                tournamentId = tournament.t.id,
                                date = date,
                                hoops = hoopsString.toInt(),
                                hoopReached = hoopReachedString.toInt(),
                                difficulty = difficulty.trim(),
                            )
                            val ranking = mutableMapOf<String, Int>()
                            ranks.forEach { ranking[it] = ranks.indexOf(it) + 1 }
                            tournament.t.players.toMutableList().apply { removeAll(ranks) }
                                .forEach { ranking[it] = 0 }
                            g.ranking = ranking
                            scope.launch { withContext(Dispatchers.IO) { dao.upsert(g) } }
                            navigator.pop()
                        } catch (e: NumberFormatException) {
                            scope.launch { hostState.showSnackbar(invalidNumber) }
                        }
                    }) {
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
        var dateDialogOpen by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues)) {
            val pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0f,
                pageCount = { 2 },
            )
            TabRow(pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(Res.string.details)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(Res.string.ranking)) },
                )
            }
            HorizontalPager(state = pagerState) { page ->
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
                                        text = "${stringResource(Res.string.date)}: ${
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
                                OutlinedTextField(
                                    value = hoopsString,
                                    onValueChange = {
                                        try {
                                            if (it != "") it.toInt()
                                            hoopsString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch { hostState.showSnackbar(invalidNumber) }
                                        }
                                    },
                                    singleLine = true,
                                    label = { Text(stringResource(Res.string.hoops)) },
                                    supportingText = { Text(stringResource(Res.string.hoops_desc)) },
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
                                            scope.launch { hostState.showSnackbar(invalidNumber) }
                                        }
                                    },
                                    singleLine = true,
                                    label = { Text(stringResource(Res.string.hoop_reached)) },
                                    supportingText = {
                                        Text(stringResource(Res.string.hoop_reached_desc))
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
                                label = { Text(stringResource(Res.string.difficulty)) },
                                placeholder = { Text(stringResource(Res.string.difficulty_placeholder)) },
                                modifier = Modifier.fillMaxWidth().padding(normalPadding),
                            )
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(tournament.t.players) {
                            var expanded by remember { mutableStateOf(false) }
                            Box(Modifier.padding(normalPadding)) {
                                OutlinedButton(
                                    onClick = { expanded = !expanded },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(normalDp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(normalDp / 2),
                                    ) {
                                        Text(
                                            text = selectedPlayers[tournament.t.players.indexOf(it)],
                                            modifier = Modifier.weight(2f),
                                        )
                                        Icon(
                                            imageVector = if (expanded) {
                                                Icons.Default.ArrowDropUp
                                            } else {
                                                Icons.Default.ArrowDropDown
                                            },
                                            contentDescription = null,
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.heightIn(max = 300.dp).fillParentMaxWidth(),
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
                                                if (selectedPlayers[tournament.t.players.indexOf(
                                                        it
                                                    )] != "---"
                                                ) {
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
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = date,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis in tournament.t.start..tournament.t.end
                },
            )
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
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({ dateDialogOpen = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState)
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
                        Text(stringResource(Res.string.delete_game))
                    }
                },
                dismissButton = {
                    TextButton({ deleteDialogOpen = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
                icon = { Icon(Icons.Default.Delete, null) },
                title = { Text("${stringResource(Res.string.delete_game)}?") },
                text = { Text(stringResource(Res.string.delete_game_hint)) },
            )
        }
        InfoDialog(showInfo)
    }
}
