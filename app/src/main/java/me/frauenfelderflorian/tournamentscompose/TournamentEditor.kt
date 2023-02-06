package me.frauenfelderflorian.tournamentscompose

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(
    navController: NavController,
    theme: Int,
    tournaments: MutableList<Tournament>,
    current: Int,
    defaultPlayers: List<String>,
    defaultAdaptivePoints: Boolean,
    defaultFirstPoints: Int
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    var name by rememberSaveable { mutableStateOf(if (current == -1) "" else tournaments[current].name) }
    var start by rememberSaveable { mutableStateOf(if (current == -1) GregorianCalendar() else tournaments[current].start) }
    var end by rememberSaveable { mutableStateOf(if (current == -1) GregorianCalendar() else tournaments[current].end) }
    if (start.after(end)) end =
        start.clone() as GregorianCalendar //better after "OK" in date picker, with SnackBar
    var useDefaults by rememberSaveable { mutableStateOf(true) }
    val players = rememberMutableStateListOf<String>()
    var adaptivePoints by rememberSaveable { mutableStateOf(true) }
    var firstPointsString by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val newPlayers =
            navController.currentBackStackEntry?.savedStateHandle?.get<String>("players")
        if (newPlayers != null) {
            players.clear()
            newPlayers.split(";").forEach { players.add(it) }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("players")
        }
    }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.edit_tournament)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (current != -1)
                            IconButton(onClick = {
                                tournaments.remove(tournaments[current])
                                navController.popBackStack() //TODO: fix from TournamentViewer
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.delete_tournament)
                                )
                            }
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (current == -1) {
                                if (!useDefaults && players.size < 2 || useDefaults && defaultPlayers.size < 2) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(
                                                R.string.at_least_two_players
                                            )
                                        )
                                    }
                                    return@IconButton
                                }
                                val t: Tournament
                                if (useDefaults)
                                    t = Tournament(
                                        start = start,
                                        end = end,
                                        players = defaultPlayers.toMutableList(),
                                        useAdaptivePoints = defaultAdaptivePoints,
                                        firstPoints = defaultFirstPoints
                                    )
                                else if (adaptivePoints)
                                    t = Tournament(
                                        start = start,
                                        end = end,
                                        players = players,
                                        useAdaptivePoints = true
                                    )
                                else if (firstPointsString.toIntOrNull() != null)
                                    t = Tournament(
                                        start = start,
                                        end = end,
                                        players = players,
                                        useAdaptivePoints = adaptivePoints,
                                        firstPoints = firstPointsString.toInt()
                                    )
                                else {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(
                                                R.string.enter_number_first_points
                                            )
                                        )
                                    }
                                    return@IconButton
                                }
                                t.name = name
                                tournaments.add(t)
                            } else {
                                tournaments[current].name = name
                                tournaments[current].start = start
                                tournaments[current].end = end
                            }
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = hostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        TextField(
                            value = name,
                            onValueChange = { if (it.length < 50) name = it },
                            singleLine = true,
                            label = { Text(stringResource(R.string.name)) },
                            placeholder = { Text(stringResource(R.string.give_meaningful_name)) },
                            trailingIcon = { Icon(Icons.Default.Edit, null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        val context = LocalContext.current
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(
                                        context, R.style.Theme_TournamentsCompose_Dialog,
                                        { _, y, m, d -> start = GregorianCalendar(y, m, d) },
                                        start.get(Calendar.YEAR),
                                        start.get(Calendar.MONTH),
                                        start.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.start_date) + ": " +
                                            formatDate(start)
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(
                                        context, R.style.Theme_TournamentsCompose_Dialog,
                                        { _, y, m, d -> end = GregorianCalendar(y, m, d) },
                                        end.get(Calendar.YEAR),
                                        end.get(Calendar.MONTH),
                                        end.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.end_date) + ": " +
                                            formatDate(end)
                                )
                            }
                        }
                    }
                    if (current == -1) {
                        item {
                            Row(modifier = Modifier.clickable { useDefaults = !useDefaults }) {
                                Text(
                                    text = stringResource(R.string.use_defaults),
                                    modifier = Modifier
                                        .weight(2f)
                                        .align(Alignment.CenterVertically)
                                )
                                Switch(
                                    checked = useDefaults,
                                    onCheckedChange = { useDefaults = it })
                            }
                        }
                        item {
                            AnimatedVisibility(
                                visible = !useDefaults,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Row {
                                    Text(
                                        text = stringResource(R.string.players) + ": "
                                                + players.joinToString(", "),
                                        modifier = Modifier
                                            .weight(2f)
                                            .align(Alignment.CenterVertically)
                                    )
                                    IconButton(onClick = {
                                        navController.navigate(
                                            route = Routes.PLAYERS_EDITOR.route +
                                                    if (players.isNotEmpty())
                                                        "?players=" + players.joinToString(";")
                                                    else ""
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            stringResource(R.string.edit_players)
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(
                                visible = !useDefaults,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Row(modifier = Modifier.clickable {
                                    adaptivePoints = !adaptivePoints
                                }) {
                                    Column(
                                        modifier = Modifier
                                            .weight(2f)
                                            .align(Alignment.CenterVertically)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.point_system) + ": " +
                                                    if (adaptivePoints)
                                                        stringResource(R.string.adaptive)
                                                    else stringResource(R.string.classic)
                                        )
                                        Text(
                                            text = if (adaptivePoints)
                                                stringResource(R.string.point_system_adaptive)
                                            else stringResource(R.string.point_system_classic),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Switch(
                                        checked = adaptivePoints,
                                        onCheckedChange = { adaptivePoints = it }
                                    )
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(
                                visible = !useDefaults && adaptivePoints,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Text(
                                    text = stringResource(R.string.point_system_adaptive_desc),
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                        item {
                            AnimatedVisibility(
                                visible = !useDefaults && !adaptivePoints,
                                enter = expandVertically(expandFrom = Alignment.Top),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Column {
                                    val context = LocalContext.current
                                    TextField(
                                        value = firstPointsString,
                                        onValueChange = {
                                            try {
                                                it.toInt()
                                                firstPointsString = it.trim()
                                            } catch (e: NumberFormatException) {
                                                scope.launch {
                                                    hostState.showSnackbar(
                                                        context.resources.getString(
                                                            R.string.no_invalid_integer
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        label = { Text(stringResource(R.string.first_points)) },
                                        trailingIcon = { Icon(Icons.Default.Star, null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = stringResource(R.string.point_system_classic_desc),
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Divider(thickness = 1.dp)
                        }
                        item {
                            Button(
                                onClick = {
                                    tournaments.remove(tournaments[current])
                                    navController.popBackStack() //TODO: fix from TournamentViewer
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.delete_tournament))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(
        saver = listSaver(
            save = {
                if (it.isNotEmpty()) {
                    val first = it.first()
                    if (!canBeSaved(first)) {
                        throw IllegalStateException(
                            "${first::class} cannot be saved. By default only types which can be " +
                                    "stored in the Bundle class can be saved."
                        )
                    }
                }
                it.toList()
            },
            restore = { it.toMutableStateList() }
        )
    ) {
        elements.toList().toMutableStateList()
    }
}
