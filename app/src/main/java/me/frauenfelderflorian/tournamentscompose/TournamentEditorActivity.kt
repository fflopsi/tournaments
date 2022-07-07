package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

const val ROUTE_TOURNAMENT_EDITOR = "te"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditor(navController: NavController) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(scrollState, true)
                ) {
                    Text(text = "Edit here")
                    ElevatedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Back")
                    }
                    for (i in 0..50) {
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
