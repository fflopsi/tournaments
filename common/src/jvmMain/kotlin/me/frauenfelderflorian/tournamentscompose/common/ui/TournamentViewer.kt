package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
actual fun TournamentViewer(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState()
    val showInfo = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(MR.strings.tournament_title, tournament.t.name),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton { navigator.pop() } },
                actions = {
                    IconButton({ navigator.push(Screen.TournamentEditor) }) {
                        Icon(Icons.Default.Edit, stringResource(MR.strings.edit_tournament))
                    }
                    IconButton(onClick = { /*TODO*/ }, enabled = false) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            stringResource(MR.strings.export_tournament_to_file)
                        )
                    }
                    SettingsInfoMenu(
                        navigateToSettings = {
                            navigator.push(Screen.AppSettings)
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
                    text = { Text(stringResource(MR.strings.new_game)) },
                    expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                    onClick = {
                        tournament.current = null
                        navigator.push(Screen.GameEditor)
                    },
                )
            }
        },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        TournamentViewerContent(
            navigator = navigator,
            tournament = tournament,
            pagerState = pagerState,
            scope = scope,
            modifier = Modifier.padding(paddingValues),
        )
        InfoDialog(showInfo)
    }
}
