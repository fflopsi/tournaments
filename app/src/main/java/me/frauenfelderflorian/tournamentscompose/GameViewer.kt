package me.frauenfelderflorian.tournamentscompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import me.frauenfelderflorian.tournamentscompose.data.Game
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameViewer(
    navController: NavController,
    theme: Int,
    game: Game
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    TournamentsComposeTheme(darkTheme = getTheme(theme = theme)) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.game_title, formatDate(game.date)))
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    stringResource(
                        R.string.game_details,
                        game.hoopReached,
                        game.hoops,
                        game.difficulty
                    )
                )
                Divider()
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 32.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, null)
                            Text(
                                game.playersByRank[0],
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        }
                    }
                    items(game.playersByRank.toMutableList().apply { removeAt(0) }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(game.ranking[it].toString())
                            Text(
                                it,
                                fontWeight = if (game.ranking[it]!! < 4) FontWeight.SemiBold else FontWeight.Light,
                                fontSize = if (game.ranking[it]!! < 4) 20.sp else 16.sp
                            )
                        }
                    }
                    item {
                        Divider()
                    }
                    item {
                        Text(stringResource(R.string.absent_players))
                    }
                    items(game.absentPlayers.toList()) {
                        Text(it, fontWeight = FontWeight.ExtraLight)
                    }
                }
            }
        }
    }
}
