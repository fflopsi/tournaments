package me.frauenfelderflorian.tournamentscompose

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme
import java.util.*

const val ROUTE_TOURNAMENT_EDITOR = "te"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(navController: NavController, tournaments: MutableList<Tournament>) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    val name = rememberSaveable { mutableStateOf("") }
    val date = rememberSaveable { mutableStateOf("") }

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
                            scope.launch {
                                hostState.showSnackbar(
                                    "test",
                                    duration = SnackbarDuration.Short
                                )
                            }
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
                            value = name.value,
                            onValueChange = { name.value = it },
                            singleLine = true,
                            label = { Text("Name") },
                            placeholder = { Text(text = "Give it a meaningful name") },
                            trailingIcon = { Icon(Icons.Default.Edit, "Edit this") },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    item {
                        val context = LocalContext.current
                        TextButton(onClick = {
                            DatePickerDialog(
                                context, R.style.Theme_TournamentsCompose_Dialog,
                                { _, year, month, day -> date.value = "$year/${month + 1}/$day" },
                                2022,
                                7,
                                11
                            ).show()
                        }) {
                            Text(text = "Start Date: ${date.value}")
                        }
                    }
                    item {
                        Text(text = "Edit here")
                    }
                    item {
                        ElevatedButton(
                            onClick = {
                                val t = Tournament(Date(), Date(), mutableListOf(""), true)
                                t.name = name.value
                                tournaments.add(t)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Back")
                        }
                    }
                    items(50) {
                        Text(text = "Edit here")
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
