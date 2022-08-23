package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsEditor(
    navController: NavController,
    theme: Int,
    formerPlayers: List<String>,
    updateTheme: (Int) -> Unit,
    savePlayers: (List<String>) -> Unit,
) {
    var themeSelectorExpanded by remember { mutableStateOf(false) }
    val players = rememberMutableStateListOf(*formerPlayers.toTypedArray())

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
                SmallTopAppBar(
                    title = { Text(text = "Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            savePlayers(players)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, "Save and exit")
                        }
                    }
                )
            }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { themeSelectorExpanded = true }
                        ) {
                            Text(
                                text = "Choose Theme: ${
                                    when (theme) {
                                        1 -> "Light"
                                        2 -> "Dark"
                                        else -> "Auto"
                                    }
                                }",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box {
                                IconButton(onClick = { themeSelectorExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, "Choose Theme")
                                }
                                DropdownMenu(
                                    expanded = themeSelectorExpanded,
                                    onDismissRequest = { themeSelectorExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = "Auto") },
                                        onClick = { updateTheme(0) },
                                        leadingIcon = {
                                            Icon(Icons.Default.BrightnessAuto, "Auto")
                                        },
                                        trailingIcon = {
                                            if (theme == 0) Icon(Icons.Default.Check, "Active")
                                        }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text(text = "Light") },
                                        onClick = { updateTheme(1) },
                                        leadingIcon = { Icon(Icons.Default.LightMode, "Light") },
                                        trailingIcon = {
                                            if (theme == 1) Icon(Icons.Default.Check, "Active")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "Dark") },
                                        onClick = { updateTheme(2) },
                                        leadingIcon = { Icon(Icons.Default.DarkMode, "Dark") },
                                        trailingIcon = {
                                            if (theme == 2) Icon(Icons.Default.Check, "Active")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Divider()
                    }
                    item {
                        Row {
                            Text(
                                text = "Default players: ${players.joinToString(", ")}",
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
            }
        }
    }
}
