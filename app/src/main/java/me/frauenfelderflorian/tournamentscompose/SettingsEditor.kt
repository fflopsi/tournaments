package me.frauenfelderflorian.tournamentscompose

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsEditor(
    navController: NavController,
    theme: Int,
    updateTheme: (Int) -> Unit,
    formerPlayers: List<String>,
    formerAdaptivePoints: Boolean,
    formerFirstPoints: Int,
    savePrefs: (List<String>, Boolean, Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    var themeSelectorExpanded by remember { mutableStateOf(false) }

    val players = rememberMutableStateListOf(*formerPlayers.toTypedArray())
    var adaptivePoints by rememberSaveable { mutableStateOf(formerAdaptivePoints) }
    var firstPointsString by rememberSaveable { mutableStateOf(formerFirstPoints.toString()) }

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
                            if (firstPointsString == "") {
                                if (!adaptivePoints)
                                    scope.launch {
                                        hostState.showSnackbar("Input a number for points for first place")
                                    }
                                else {
                                    savePrefs(players, adaptivePoints, 10)
                                    navController.popBackStack()
                                }
                            } else {
                                savePrefs(players, adaptivePoints, firstPointsString.toInt())
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Check, "Save and exit")
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
                    item {
                        Row(modifier = Modifier.clickable { adaptivePoints = !adaptivePoints }) {
                            Column(
                                modifier = Modifier
                                    .weight(2f)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = "Default point system: "
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
                    item {
                        AnimatedVisibility(
                            visible = adaptivePoints,
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
                            visible = !adaptivePoints,
                            enter = expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            Column {
                                TextField(
                                    value = firstPointsString,
                                    onValueChange = {
                                        try {
                                            if (it != "")
                                                it.toInt()
                                            firstPointsString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch {
                                                hostState.showSnackbar("Input a valid integer")
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
            }
        }
    }
}
