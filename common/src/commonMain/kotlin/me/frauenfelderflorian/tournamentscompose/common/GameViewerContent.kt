package me.frauenfelderflorian.tournamentscompose.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun GameViewerContent(game: Game, modifier: Modifier) {
    Column(modifier) {
        Text(
            text = stringResource(
                MR.strings.game_details, game.hoopReached, game.hoops, game.difficulty
            ),
            modifier = Modifier.padding(normalPadding),
        )
        Divider()
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
            item { Divider() }
            item { Text(stringResource(MR.strings.absent_players)) }
            items(game.absentPlayers.toList()) {
                Text(text = it, fontWeight = FontWeight.ExtraLight)
            }
        }
    }
}
