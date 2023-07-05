package me.frauenfelderflorian.tournamentscompose.common.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.data.playersByRank
import me.frauenfelderflorian.tournamentscompose.common.data.ranking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerViewer(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    player: String,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        "${stringResource(MR.strings.game_overview_for)} $player", scrollBehavior
                    )
                },
                navigationIcon = { BackButton(navigator) },
                actions = { SettingsInfoMenu(navigator = navigator, showInfoDialog = showInfo) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(normalDp),
            horizontalArrangement = Arrangement.spacedBy(normalDp),
            modifier = Modifier.padding(paddingValues),
        ) {
            items(tournament.games.sortedByDescending { it.date }) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(normalDp),
                    modifier = Modifier.padding(normalPadding),
                ) {
                    Text(formatDate(it.date))
                    Text(
                        text = if (it.ranking[player] == 0) {
                            stringResource(MR.strings.absent)
                        } else {
                            stringResource(
                                MR.strings.rank_of,
                                it.ranking[player]!!,
                                it.playersByRank.size,
                            )
                        },
                        style = titleStyle,
                    )
                    Divider()
                    Text(it.difficulty)
                    Text(stringResource(MR.strings.hoop_of_reached, it.hoopReached, it.hoops))
                }
            }
        }
        InfoDialog(showInfo)
    }
}