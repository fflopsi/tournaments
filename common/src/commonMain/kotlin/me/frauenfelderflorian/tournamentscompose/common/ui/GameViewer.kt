package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import me.frauenfelderflorian.tournamentscompose.common.data.Game
import me.frauenfelderflorian.tournamentscompose.common.data.absentPlayers
import me.frauenfelderflorian.tournamentscompose.common.data.playersByRank
import me.frauenfelderflorian.tournamentscompose.common.data.ranking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun GameViewer(
    navigator: StackNavigation<Screen>,
    game: Game,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(Res.string.game_title, formatDate(game.date)),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton(navigator) },
                actions = {
                    IconButton({ navigator.push(Screen.GameEditor) }) {
                        Icon(Icons.Default.Edit, stringResource(Res.string.edit_game))
                    }
                    SettingsInfoMenu(navigator = navigator, showInfoDialog = showInfo)
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Text(
                text = stringResource(
                    Res.string.game_details, game.hoopReached, game.hoops, game.difficulty
                ),
                modifier = Modifier.padding(normalPadding),
            )
            HorizontalDivider()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(normalDp),
                horizontalAlignment = Alignment.Start,
                contentPadding = PaddingValues(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(normalPadding),
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.EmojiEvents, null)
                        Text(
                            text = game.playersByRank[0],
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                        )
                    }
                }
                items(game.playersByRank.toMutableList().apply { removeAt(0) }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(game.ranking[it].toString())
                        Text(
                            text = it,
                            fontWeight = if (game.ranking[it]!! < 4) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Light
                            },
                            fontSize = if (game.ranking[it]!! < 4) 20.sp else 16.sp,
                        )
                    }
                }
                item { HorizontalDivider() }
                item { Text(stringResource(Res.string.absent_players)) }
                items(game.absentPlayers.toList()) {
                    Text(text = it, fontWeight = FontWeight.ExtraLight)
                }
            }
        }
        InfoDialog(showInfo)
    }
}
