package me.frauenfelderflorian.tournamentscompose.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.getTheme
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettings(
    navController: NavController,
    theme: Int,
    updateTheme: (Int) -> Unit,
    dynamicColor: Boolean,
    updateDynamicColor: (Boolean) -> Unit,
    formerPlayers: List<String>,
    formerAdaptivePoints: Boolean,
    formerFirstPoints: Int,
    savePrefs: (List<String>, Boolean, Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
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

    TournamentsTheme(darkTheme = getTheme(theme), dynamicColor = dynamicColor) {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton({ navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton({
                            if (firstPointsString == "") {
                                if (!adaptivePoints) {
                                    scope.launch {
                                        hostState.showSnackbar(
                                            context.resources.getString(R.string.input_first_points)
                                        )
                                    }
                                } else {
                                    savePrefs(players, true, 10)
                                    navController.popBackStack()
                                }
                            } else {
                                savePrefs(players, adaptivePoints, firstPointsString.toInt())
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Check, stringResource(R.string.save_and_exit))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = { SnackbarHost(hostState) },
            contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues),
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { themeSelectorExpanded = true },
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
                                .weight(2f),
                        )
                        Box {
                            IconButton({ themeSelectorExpanded = true }) {
                                Icon(Icons.Default.MoreVert, null)
                            }
                            DropdownMenu(
                                expanded = themeSelectorExpanded,
                                onDismissRequest = { themeSelectorExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.auto)) },
                                    onClick = { updateTheme(0) },
                                    leadingIcon = { Icon(Icons.Default.BrightnessAuto, null) },
                                    trailingIcon = {
                                        if (theme == 0) {
                                            Icon(
                                                Icons.Default.Check, stringResource(R.string.active)
                                            )
                                        }
                                    },
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.light)) },
                                    onClick = { updateTheme(1) },
                                    leadingIcon = { Icon(Icons.Default.LightMode, null) },
                                    trailingIcon = {
                                        if (theme == 1) {
                                            Icon(
                                                Icons.Default.Check, stringResource(R.string.active)
                                            )
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.dark)) },
                                    onClick = { updateTheme(2) },
                                    leadingIcon = { Icon(Icons.Default.DarkMode, null) },
                                    trailingIcon = {
                                        if (theme == 2) {
                                            Icon(
                                                Icons.Default.Check, stringResource(R.string.active)
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { updateDynamicColor(!dynamicColor) }
                        ) {
                            Text(
                                text = stringResource(R.string.use_dynamic_color),
                                modifier = Modifier.weight(2f),
                            )
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = { updateDynamicColor(it) })
                        }
                    }
                }
                item { Divider() }
                item {
                    Row {
                        Text(
                            text = "${stringResource(R.string.default_players)}: ${
                                players.joinToString(", ")
                            }",
                            modifier = Modifier
                                .weight(2f)
                                .align(Alignment.CenterVertically),
                        )
                        IconButton({
                            navController.navigate(
                                "${Routes.PLAYERS_EDITOR.route}${
                                    if (players.isNotEmpty()) {
                                        "?players=${players.joinToString(";")}"
                                    } else {
                                        ""
                                    }
                                }"
                            )
                        }) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit_players))
                        }
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.clickable { adaptivePoints = !adaptivePoints },
                    ) {
                        Column(
                            Modifier
                                .weight(2f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(
                                "${stringResource(R.string.default_point_system)}: ${
                                    if (adaptivePoints) {
                                        stringResource(R.string.adaptive)
                                    } else {
                                        stringResource(R.string.classic)
                                    }
                                }"

                            )
                            Text(
                                text = if (adaptivePoints) {
                                    stringResource(R.string.point_system_adaptive)
                                } else {
                                    stringResource(R.string.point_system_classic)
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                        Switch(checked = adaptivePoints, onCheckedChange = { adaptivePoints = it })
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = adaptivePoints,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        Text(
                            text = stringResource(R.string.point_system_adaptive_desc),
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = !adaptivePoints,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        Column {
                            val context = LocalContext.current
                            TextField(
                                value = firstPointsString,
                                onValueChange = {
                                    try {
                                        if (it != "") it.toInt()
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
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                text = stringResource(R.string.point_system_classic_desc),
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }
                }
            }
        }
    }
}
