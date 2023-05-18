package me.frauenfelderflorian.tournamentscompose.android.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.android.R
import me.frauenfelderflorian.tournamentscompose.common.Routes
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.ui.BackButton
import me.frauenfelderflorian.tournamentscompose.common.ui.InfoDialog
import me.frauenfelderflorian.tournamentscompose.common.ui.SettingsInfoMenu
import me.frauenfelderflorian.tournamentscompose.common.ui.TopAppBarTitle
import me.frauenfelderflorian.tournamentscompose.common.ui.detailsStyle
import me.frauenfelderflorian.tournamentscompose.common.ui.exportToUri
import me.frauenfelderflorian.tournamentscompose.common.ui.formatDate
import me.frauenfelderflorian.tournamentscompose.common.ui.normalDp
import me.frauenfelderflorian.tournamentscompose.common.ui.normalPadding
import me.frauenfelderflorian.tournamentscompose.common.ui.titleStyle

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun TournamentViewer(
    navController: NavController,
    tournament: TournamentWithGames,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState()
    val showInfo = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val exportToFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(stringResource(R.string.file_mime))
    ) {
        exportToUri(
            uri = it,
            context = context,
            scope = scope,
            hostState = hostState,
            content = setOf(tournament),
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(R.string.tournament_title, tournament.t.name),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton { navController.navigateUp() } },
                actions = {
                    IconButton({ navController.navigate(Routes.TOURNAMENT_EDITOR.route) }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                    }
                    IconButton({ exportToFile.launch(context.resources.getString(R.string.file_name_tournament)) }) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            stringResource(R.string.export_tournament_to_file)
                        )
                    }
                    SettingsInfoMenu(
                        navigateToSettings = {
                            navController.navigate(Routes.SETTINGS_EDITOR.route)
                        },
                        showInfoDialog = showInfo,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = pagerState.currentPage == 0,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.new_game)) },
                    expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                    onClick = {
                        tournament.current = null
                        navController.navigate(Routes.GAME_EDITOR.route)
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState) },
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            TabRow(pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.details)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.ranking)) },
                )
            }
            HorizontalPager(pageCount = 2, state = pagerState) { page ->
                if (page == 0) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        if (tournament.games.isNotEmpty()) {
                            items(tournament.games.sortedByDescending { it.date }) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(normalDp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            tournament.current = it
                                            navController.navigate(Routes.GAME_VIEWER.route)
                                        }
                                        .padding(normalPadding),
                                ) {
                                    Column(Modifier.weight(2f)) {
                                        Text(
                                            text = stringResource(
                                                R.string.game_title, formatDate(it.date)
                                            ),
                                            style = titleStyle,
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.game_details,
                                                it.hoopReached,
                                                it.hoops,
                                                it.difficulty,
                                            ),
                                            style = detailsStyle,
                                        )
                                    }
                                    IconButton({
                                        tournament.current = it
                                        navController.navigate(Routes.GAME_EDITOR.route)
                                    }) {
                                        Icon(Icons.Default.Edit, stringResource(R.string.edit_game))
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = stringResource(R.string.add_first_game_hint),
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.ExtraLight,
                                    modifier = Modifier.padding(normalPadding),
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(tournament.playersByPoints) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(normalDp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(normalPadding),
                            ) {
                                Text(tournament.getPoints(it).toString())
                                Text(
                                    text = it,
                                    style = titleStyle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(2f),
                                )
                                Box {
                                    IconButton({ menuExpanded = true }) {
                                        Icon(Icons.Default.MoreVert, null)
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            enabled = false,
                                            text = { Text(stringResource(R.string.game_overview)) },
                                            onClick = { /*TODO*/ },
                                            leadingIcon = { Icon(Icons.Default.EmojiEvents, null) },
                                        )
                                        Divider()
                                        DropdownMenuItem(
                                            enabled = false,
                                            text = { Text(stringResource(R.string.delete_player)) },
                                            onClick = { /*TODO*/ },
                                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        InfoDialog(showInfo)
    }
}
