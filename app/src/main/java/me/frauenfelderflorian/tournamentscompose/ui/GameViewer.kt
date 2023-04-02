package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.data.Game
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameViewer(
    navController: NavController,
    theme: Int,
    dynamicColor: Boolean,
    game: Game,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }

    TournamentsTheme(darkTheme = getTheme(theme), dynamicColor = dynamicColor) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.game_title, formatDate(game.date))) },
                    navigationIcon = {
                        IconButton({ navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    actions = {
                        SettingsInfoMenu(navController = navController, showInfoDialog = showInfo)
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp, 16.dp),
            ) {
                Text(
                    stringResource(
                        R.string.game_details, game.hoopReached, game.hoops, game.difficulty
                    )
                )
                Divider()
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(32.dp, 32.dp),
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(game.rankingMap[it].toString())
                            Text(
                                text = it,
                                fontWeight = if (game.rankingMap[it]!! < 4) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Light
                                },
                                fontSize = if (game.rankingMap[it]!! < 4) 20.sp else 16.sp,
                            )
                        }
                    }
                    item { Divider() }
                    item { Text(stringResource(R.string.absent_players)) }
                    items(game.absentPlayers.toList()) {
                        Text(text = it, fontWeight = FontWeight.ExtraLight)
                    }
                }
            }
            InfoDialog(showDialog = showInfo)
        }
    }
}
