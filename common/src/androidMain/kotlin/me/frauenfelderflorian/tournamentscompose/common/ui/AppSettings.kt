package me.frauenfelderflorian.tournamentscompose.common.ui

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.Res

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalResourceApi::class
)
@Composable
actual fun AppSettings(
    navigator: StackNavigation<Screen>,
    prefs: Prefs,
    playersModel: PlayersModel,
) {
    var firstPoints: Int? by rememberSaveable { mutableStateOf(prefs.firstPoints) }

    val context = LocalContext.current
    val hostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pagerState =
        rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f, pageCount = { 2 })
    val showInfo = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(playersModel.edited) {
        if (playersModel.edited) {
            prefs.players = listOf(*playersModel.players.toTypedArray())
            playersModel.edited = false
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { TopAppBarTitle(stringResource(Res.string.settings), scrollBehavior) },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    IconButton({ showInfo.value = true }) {
                        Icon(Icons.Outlined.Info, stringResource(Res.string.about))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            TabRow(pagerState.currentPage) {
                val scope = rememberCoroutineScope()
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(Res.string.app)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(Res.string.new_tournaments)) },
                )
            }
            HorizontalPager(state = pagerState) { page ->
                if (page == 0) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            var themeSelectorExpanded by remember { mutableStateOf(false) }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(normalDp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { themeSelectorExpanded = true }
                                    .padding(normalPadding),
                            ) {
                                Column(Modifier.weight(2f)) {
                                    Text(
                                        text = stringResource(Res.string.choose_theme),
                                        style = titleStyle
                                    )
                                    Text(
                                        text = stringResource(
                                            when (prefs.theme) {
                                                1 -> Res.string.light
                                                2 -> Res.string.dark
                                                else -> Res.string.auto
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
                                            text = { Text(stringResource(Res.string.auto)) },
                                            onClick = { prefs.theme = 0 },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.BrightnessAuto, null
                                                )
                                            },
                                            trailingIcon = {
                                                if (prefs.theme == 0) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        stringResource(Res.string.active)
                                                    )
                                                }
                                            },
                                        )
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text(stringResource(Res.string.light)) },
                                            onClick = { prefs.theme = 1 },
                                            leadingIcon = { Icon(Icons.Default.LightMode, null) },
                                            trailingIcon = {
                                                if (prefs.theme == 1) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        stringResource(Res.string.active)
                                                    )
                                                }
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(Res.string.dark)) },
                                            onClick = { prefs.theme = 2 },
                                            leadingIcon = { Icon(Icons.Default.DarkMode, null) },
                                            trailingIcon = {
                                                if (prefs.theme == 2) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        stringResource(Res.string.active)
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
                                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        prefs.dynamicColor = !prefs.dynamicColor
                                    }.padding(normalPadding),
                                ) {
                                    Column(Modifier.weight(2f)) {
                                        Text(
                                            text = stringResource(Res.string.use_dynamic_color),
                                            style = titleStyle
                                        )
                                        Text(
                                            text = stringResource(
                                                if (prefs.dynamicColor) {
                                                    Res.string.dynamic_color_on_desc
                                                } else {
                                                    Res.string.dynamic_color_off_desc
                                                }
                                            ),
                                            style = detailsStyle,
                                        )
                                    }
                                    Switch(checked = prefs.dynamicColor, onCheckedChange = null)
                                }
                            }
                        }
                        item { HorizontalDivider() }
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(normalDp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    prefs.experimentalFeatures = !prefs.experimentalFeatures
                                }.padding(normalPadding),
                            ) {
                                Column(Modifier.weight(2f)) {
                                    Text(
                                        text = stringResource(Res.string.experimental_features),
                                        style = titleStyle,
                                    )
                                    Text(
                                        text = stringResource(Res.string.experimental_features_desc),
                                        style = detailsStyle,
                                    )
                                }
                                Switch(checked = prefs.experimentalFeatures, onCheckedChange = null)
                            }
                        }
                        item { HorizontalDivider() }
                        item {
                            Column(Modifier.clickable {
                                context.startActivity(
                                    Intent.makeRestartActivityTask(
                                        context.packageManager.getLaunchIntentForPackage(
                                            context.packageName
                                        )!!.component!!
                                    )
                                )
                                Runtime.getRuntime().exit(0)
                            }.fillMaxWidth().padding(normalPadding)) {
                                Text(text = stringResource(Res.string.restart), style = titleStyle)
                                Text(
                                    text = stringResource(Res.string.restart_desc),
                                    style = detailsStyle,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = stringResource(Res.string.default_tournament_settings),
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(normalPadding),
                            )
                        }
                        item {
                            val player = stringResource(Res.string.player)
                            PlayersSetting(prefs.players) {
                                playersModel.players.clear()
                                playersModel.players.addAll(prefs.players.ifEmpty {
                                    listOf("$player 1", "$player 2")
                                })
                                navigator.push(Screen.PlayersEditor)
                            }
                        }
                        item {
                            val scope = rememberCoroutineScope()
                            PointSystemSettings(
                                adaptivePoints = prefs.adaptivePoints,
                                onClickAdaptivePoints = {
                                    prefs.adaptivePoints = !prefs.adaptivePoints
                                },
                                firstPoints = firstPoints,
                                onChangeFirstPoints = {
                                    try {
                                        if (it != "") it.toInt()
                                        firstPoints = it.toIntOrNull()
                                    } catch (e: NumberFormatException) {
                                        scope.launch {
                                            hostState.showSnackbar(
                                                getString(Res.string.invalid_number)
                                            )
                                        }
                                    }
                                    prefs.firstPoints = firstPoints ?: 10
                                },
                            )
                        }
                    }
                }
            }
        }
        InfoDialog(showInfo)
    }
}
