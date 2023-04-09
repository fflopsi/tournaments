package me.frauenfelderflorian.tournamentscompose.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.R

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
    val players = rememberMutableStateListOf(*formerPlayers.toTypedArray())
    val adaptivePoints = rememberSaveable { mutableStateOf(formerAdaptivePoints) }
    val firstPoints: MutableState<Int?> = rememberSaveable { mutableStateOf(formerFirstPoints) }

    LaunchedEffect(Unit) {
        val newPlayers =
            navController.currentBackStackEntry?.savedStateHandle?.get<String>("players")
        if (newPlayers != null) {
            players.clear()
            newPlayers.split(";").forEach { players.add(it) }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("players")
            if (adaptivePoints.value || firstPoints.value == null) {
                savePrefs(players, adaptivePoints.value, 10)
            } else {
                savePrefs(players, false, firstPoints.value!!)
            }
        }
    }

    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(R.string.settings), scrollBehavior) },
                navigationIcon = { BackButton(navController) },
                actions = {
                    IconButton({ showInfo.value = true }) {
                        Icon(Icons.Outlined.Info, stringResource(R.string.about))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            var themeSelectorExpanded by remember { mutableStateOf(false) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(normalDp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { themeSelectorExpanded = true }
                    .padding(normalPadding),
            ) {
                Column(Modifier.weight(2f)) {
                    Text(text = stringResource(R.string.choose_theme), style = titleStyle)
                    Text(
                        text = stringResource(
                            when (theme) {
                                1 -> R.string.light
                                2 -> R.string.dark
                                else -> R.string.auto
                            }
                        ),
                        style = detailsStyle,
                    )
                }
                Box {
                    Icon(Icons.Default.MoreVert, null)
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
                                    Icon(Icons.Default.Check, stringResource(R.string.active))
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
                                    Icon(Icons.Default.Check, stringResource(R.string.active))
                                }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dark)) },
                            onClick = { updateTheme(2) },
                            leadingIcon = { Icon(Icons.Default.DarkMode, null) },
                            trailingIcon = {
                                if (theme == 2) {
                                    Icon(Icons.Default.Check, stringResource(R.string.active))
                                }
                            },
                        )
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { updateDynamicColor(!dynamicColor) }
                        .padding(normalPadding),
                ) {
                    Column(Modifier.weight(2f)) {
                        Text(text = stringResource(R.string.use_dynamic_color), style = titleStyle)
                        Text(
                            text = stringResource(
                                if (dynamicColor) {
                                    R.string.dynamic_color_on_desc
                                } else {
                                    R.string.dynamic_color_off_desc
                                }
                            ),
                            style = detailsStyle,
                        )
                    }
                    Switch(checked = dynamicColor, onCheckedChange = null)
                }
            }
            Divider()
            Text(
                text = stringResource(R.string.default_tournament_settings),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(normalPadding),
            )
            PlayersSetting(navController = navController, players = players)
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            PointSystemSettings(
                adaptivePoints = adaptivePoints,
                onClickAdaptivePoints = {
                    adaptivePoints.value = !adaptivePoints.value
                    if (adaptivePoints.value || firstPoints.value == null) {
                        savePrefs(players, adaptivePoints.value, 10)
                    } else {
                        savePrefs(players, false, firstPoints.value!!)
                    }
                },
                firstPointsString = firstPoints,
                onChangeFirstPoints = {
                    try {
                        if (it != "") it.toInt()
                        firstPoints.value = it.toIntOrNull()
                    } catch (e: NumberFormatException) {
                        scope.launch {
                            hostState.showSnackbar(
                                context.resources.getString(R.string.invalid_number)
                            )
                        }
                    }
                    if (adaptivePoints.value || firstPoints.value == null) {
                        savePrefs(players, adaptivePoints.value, 10)
                    } else {
                        savePrefs(players, false, firstPoints.value!!)
                    }
                },
            )
        }
        InfoDialog(showDialog = showInfo)
    }
}
