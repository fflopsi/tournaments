package me.frauenfelderflorian.tournamentscompose

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
fun TournamentEditor(navController: NavController, tournaments: MutableList<Tournament>) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    var name by rememberSaveable { mutableStateOf("") }
    var start by rememberSaveable { mutableStateOf(GregorianCalendar()) }
    var end by rememberSaveable { mutableStateOf(GregorianCalendar()) }
    if (start.after(end)) end = start.clone() as GregorianCalendar //better after "OK" in date picker, with SnackBar
    var useDefaults by rememberSaveable { mutableStateOf(true) }
    var adaptivePoints by rememberSaveable { mutableStateOf(true) }
    var firstPointsString by rememberSaveable { mutableStateOf("") }

    TournamentsComposeTheme {
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
                        IconButton(onClick = {
                            val t = Tournament(Date(), Date(), mutableListOf(""), true)
                            t.name = name
                            tournaments.add(t)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, "Save and exit")
                        }
                    }
                )
            }
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
                            ElevatedButton(
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
                                    text = "Start Date: ${start.get(Calendar.YEAR)}" +
                                            "/${start.get(Calendar.MONTH) + 1}" +
                                            "/${start.get(Calendar.DAY_OF_MONTH)}"
                                )
                            }
                            ElevatedButton(
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
                                    text = "End Date: ${end.get(Calendar.YEAR)}" +
                                            "/${end.get(Calendar.MONTH) + 1}" +
                                            "/${end.get(Calendar.DAY_OF_MONTH)}"
                                )
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier.clickable { useDefaults = !useDefaults }) {
                            Text(
                                text = "Use default values",
                                modifier = Modifier
                                    .weight(2f)
                                    .align(Alignment.CenterVertically)
                            )
                            Switch(checked = useDefaults, onCheckedChange = { useDefaults = it })
                        }
                    }
                    if (!useDefaults) {
                        item {
                            Row {
                                Text(
                                    text = "Players: ",
                                    modifier = Modifier
                                        .weight(2f)
                                        .align(Alignment.CenterVertically)
                                )
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(Icons.Default.Edit, "Edit players")
                                }
                            }
                        }
                        item {
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
                        if (adaptivePoints) item {
                            Text(
                                text = "In this system, absent players never get points. The last player always gets 2 points, the second-to-last 3 points, etc. Thus, there is no fixed amount of points for first/second/... place, but it varies based on the number of players present. However, second place gets 3 points less than first place, third place gets 2 points less than second place, and fourth place gets 2 points less than third place (if applicable).",
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                        else item {
                            Column {
                                TextField(
                                    value = firstPointsString,
                                    onValueChange = {
                                        try {
                                            it.toDouble()
                                            firstPointsString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch {
                                                hostState.currentSnackbarData?.dismiss()
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
                }
                SnackbarHost(
                    hostState = hostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
