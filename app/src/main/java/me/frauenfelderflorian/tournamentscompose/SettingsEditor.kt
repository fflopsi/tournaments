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
fun SettingsEditor(navController: NavController, theme: Int, updateTheme: (Int) -> Unit) {
    var themeSelectorExpanded by remember { mutableStateOf(false) }

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = "Settings") },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { /*TODO*/ }) {
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
                                text = "Choose Theme...",
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
                                        leadingIcon = { Icon(Icons.Default.LightMode, "Auto") },
                                        trailingIcon = {
                                            if (theme == 1) Icon(Icons.Default.Check, "Active")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "Dark") },
                                        onClick = {
                                            updateTheme(2)
                                        },
                                        leadingIcon = { Icon(Icons.Default.DarkMode, "Auto") },
                                        trailingIcon = {
                                            if (theme == 2) Icon(Icons.Default.Check, "Active")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
