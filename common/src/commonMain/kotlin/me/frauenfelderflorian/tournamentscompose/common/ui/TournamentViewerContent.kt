package me.frauenfelderflorian.tournamentscompose.common.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.data.getPoints
import me.frauenfelderflorian.tournamentscompose.common.data.playersByPoints

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TournamentViewerContent(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    pagerState: PagerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        TabRow(pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text(stringResource(MR.strings.details)) },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text(stringResource(MR.strings.ranking)) },
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
                                modifier = Modifier.clickable {
                                    tournament.current = it
                                    navigator.push(Screen.GameViewer)
                                }.padding(normalPadding),
                            ) {
                                Column(Modifier.weight(2f)) {
                                    Text(
                                        text = stringResource(
                                            MR.strings.game_title, formatDate(it.date)
                                        ),
                                        style = titleStyle,
                                    )
                                    Text(
                                        text = stringResource(
                                            MR.strings.game_details,
                                            it.hoopReached,
                                            it.hoops,
                                            it.difficulty,
                                        ),
                                        style = detailsStyle,
                                    )
                                }
                                IconButton({
                                    tournament.current = it
                                    navigator.push(Screen.GameEditor)
                                }) {
                                    Icon(
                                        Icons.Default.Edit, stringResource(MR.strings.edit_game)
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(MR.strings.add_first_game_hint),
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
                                modifier = Modifier.fillMaxWidth().weight(2f),
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
                                        text = { Text(stringResource(MR.strings.game_overview)) },
                                        onClick = { /*TODO*/ },
                                        leadingIcon = { Icon(Icons.Default.EmojiEvents, null) },
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        enabled = false,
                                        text = { Text(stringResource(MR.strings.delete_player)) },
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
}