package me.frauenfelderflorian.tournamentscompose

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme
import java.util.*

const val ROUTE_TOURNAMENT_EDITOR = "te"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(navController: NavController, tournaments: MutableList<Tournament>) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    var name by rememberSaveable { mutableStateOf("") }
    var start by rememberSaveable { mutableStateOf(GregorianCalendar()) }
    var end by rememberSaveable { mutableStateOf(GregorianCalendar()) }
    if (start.after(end)) end = start.clone() as GregorianCalendar

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
                            onValueChange = { name = it },
                            singleLine = true,
                            label = { Text("Name") },
                            placeholder = { Text(text = "Give it a meaningful name") },
                            trailingIcon = { Icon(Icons.Default.Edit, "Edit this") },
                            modifier = Modifier.fillMaxSize()
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
                }
                SnackbarHost(
                    hostState = hostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
