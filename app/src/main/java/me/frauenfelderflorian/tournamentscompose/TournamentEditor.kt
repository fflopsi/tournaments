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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    current: Int
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

    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Map<UUID, String>>("players")
        ?.observeAsState()?.value?.let {
            players.clear()
            for (player in it.toList().sortedBy { (_, value) -> value.lowercase() })
                players.add(player.second)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Map<UUID, String>>("players")
        }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = "Edit Tournament") },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        if (current != -1)
                            IconButton(onClick = {
                                tournaments.remove(tournaments[current])
                                navController.popBackStack()
                            }) {
                                Icon(Icons.Default.Delete, "Delete this tournament")
                            }
                        IconButton(onClick = {
                            if (current == -1) {
                                val t = Tournament(
                                    start = start,
                                    end = end,
                                    players = players,
                                    useAdaptivePoints = adaptivePoints,
                                    firstPoints = firstPointsString.toInt()
                                )
                                t.name = name
                                tournaments.add(t)
                            } else {
                                tournaments[current].name = name
                                tournaments[current].start = start
                                tournaments[current].end = end
                            }
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, "Save and exit")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = hostState) }
        ) { paddingValues: PaddingValues ->
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
                            label = { Text("Name") },
                            placeholder = { Text(text = "Give it a meaningful name") },
                            trailingIcon = { Icon(Icons.Default.Edit, "Edit this") },
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
                                Text(text = "Start Date: ${formatDate(start)}")
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
                                Text(text = "End Date: ${formatDate(end)}")
                            }
                        }
                    }
                    if (current == -1) {
                        item {
                            Row(modifier = Modifier.clickable { useDefaults = !useDefaults }) {
                                Text(
                                    text = "Use default values",
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
                                        text = "Players: ${players.joinToString(", ")}",
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
                                        Icon(Icons.Default.Edit, "Edit players")
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
                                            text = "Point system: "
                                                    + (if (adaptivePoints) "Adaptive" else "Classic")
                                        )
                                        Text(
                                            text = if (adaptivePoints)
                                                "Recommended point system. Switch off for classic system"
                                            else "Old point system. Switch on for adaptive system",
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
                                    text = "In this system, absent players never get points. The last player always gets 2 points, the second-to-last 3 points, etc. Thus, there is no fixed amount of points for first/second/... place, but it varies based on the number of players present. However, second place gets 3 points less than first place, third place gets 2 points less than second place, and fourth place gets 2 points less than third place (if applicable).",
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
                                    TextField(
                                        value = firstPointsString,
                                        onValueChange = {
                                            try {
                                                it.toDouble()
                                                firstPointsString = it.trim()
                                            } catch (e: NumberFormatException) {
                                                scope.launch {
                                                    hostState.showSnackbar(
                                                        "Input a valid number",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        label = { Text("Points for first place") },
                                        trailingIcon = { Icon(Icons.Default.Star, "Edit this") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "Second place gets 3 points less than first place, third place gets 2 points less than second place, and fourth place gets 2 points less than third place. The system will assign negative points if necessary.",
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
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Delete this tournament")
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
            save = { stateList ->
                if (stateList.isNotEmpty()) {
                    val first = stateList.first()
                    if (!canBeSaved(first)) {
                        throw IllegalStateException("${first::class} cannot be saved. By default only types which can be stored in the Bundle class can be saved.")
                    }
                }
                stateList.toList()
            },
            restore = { it.toMutableStateList() }
        )
    ) {
        elements.toList().toMutableStateList()
    }
}
