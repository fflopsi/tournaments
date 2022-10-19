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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
                TopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (firstPointsString == "") {
                                if (!adaptivePoints)
                                    scope.launch {
                                        hostState.showSnackbar(context.resources.getString(R.string.input_first_points))
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { themeSelectorExpanded = true }
                        ) {
                            Text(
                                text = "${stringResource(R.string.choose_theme)}: ${
                                    when (theme) {
                                        1 -> stringResource(R.string.light)
                                        2 -> stringResource(R.string.dark)
                                        else -> stringResource(R.string.auto)
                                    }
                                }",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box {
                                IconButton(onClick = { themeSelectorExpanded = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        stringResource(R.string.choose_theme)
                                    )
                                }
                                DropdownMenu(
                                    expanded = themeSelectorExpanded,
                                    onDismissRequest = { themeSelectorExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.auto)) },
                                        onClick = { updateTheme(0) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.BrightnessAuto,
                                                stringResource(R.string.auto)
                                            )
                                        },
                                        trailingIcon = {
                                            if (theme == 0) Icon(
                                                Icons.Default.Check,
                                                stringResource(R.string.active)
                                            )
                                        }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.light)) },
                                        onClick = { updateTheme(1) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.LightMode,
                                                stringResource(R.string.light)
                                            )
                                        },
                                        trailingIcon = {
                                            if (theme == 1)
                                                Icon(
                                                    Icons.Default.Check,
                                                    stringResource(R.string.active)
                                                )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.dark)) },
                                        onClick = { updateTheme(2) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.DarkMode,
                                                stringResource(R.string.dark)
                                            )
                                        },
                                        trailingIcon = {
                                            if (theme == 2)
                                                Icon(
                                                    Icons.Default.Check,
                                                    stringResource(R.string.active)
                                                )
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
                                text = stringResource(R.string.default_players) + ": " +
                                        players.joinToString(", "),
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
                                Icon(Icons.Default.Edit, stringResource(R.string.edit_players))
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
                                    text = stringResource(R.string.default_point_system)
                                            + ": " +
                                            if (adaptivePoints) stringResource(R.string.adaptive)
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
                    item {
                        AnimatedVisibility(
                            visible = adaptivePoints,
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
                            visible = !adaptivePoints,
                            enter = expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            Column {
                                val context = LocalContext.current
                                TextField(
                                    value = firstPointsString,
                                    onValueChange = {
                                        try {
                                            if (it != "")
                                                it.toInt()
                                            firstPointsString = it.trim()
                                        } catch (e: NumberFormatException) {
                                            scope.launch {
                                                hostState.showSnackbar(context.resources.getString(R.string.no_invalid_integer))
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
                }
            }
        }
    }
}
