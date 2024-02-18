package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import me.frauenfelderflorian.tournamentscompose.common.data.getPoints
import me.frauenfelderflorian.tournamentscompose.common.data.playersByPoints
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun TournamentViewerContent(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    showDeletePlayerDialog: MutableState<Boolean>,
    playerToBeDeleted: MutableState<String>,
    showRenamePlayerDialog: MutableState<Boolean>,
    playerToBeRenamed: MutableState<String>,
    pagerState: PagerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        TabRow(pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text(stringResource(Res.string.details)) },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text(stringResource(Res.string.ranking)) },
            )
        }
        HorizontalPager(state = pagerState) { page ->
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
                                            Res.string.game_title, formatDate(it.date)
                                        ),
                                        style = titleStyle,
                                    )
                                    Text(
                                        text = stringResource(
                                            Res.string.game_details,
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
                                        Icons.Default.Edit, stringResource(Res.string.edit_game)
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(64.dp)) }
                    } else {
                        item {
                            Text(
                                text = stringResource(Res.string.add_first_game_hint),
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
                            modifier = Modifier.clickable { navigator.push(Screen.PlayerViewer(it)) }
                                .padding(normalPadding),
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
                                        text = { Text(stringResource(Res.string.rename_player)) },
                                        onClick = {
                                            playerToBeRenamed.value = it
                                            showRenamePlayerDialog.value = true
                                            menuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.delete_player)) },
                                        onClick = {
                                            playerToBeDeleted.value = it
                                            showDeletePlayerDialog.value = true
                                            menuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }
}