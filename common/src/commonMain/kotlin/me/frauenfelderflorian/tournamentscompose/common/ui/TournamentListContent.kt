package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames
import java.util.UUID

@Composable
fun TournamentListContent(
    tournaments: Map<UUID, TournamentWithGames>,
    setCurrent: (UUID?) -> Unit,
    navigator: StackNavigation<Screen>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        if (tournaments.isNotEmpty()) {
            items(tournaments.values.sortedByDescending { it.t.start }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        setCurrent(it.t.id)
                        navigator.push(Screen.TournamentViewer)
                    }.padding(normalPadding),
                ) {
                    Column(Modifier.weight(2f)) {
                        Text(text = it.t.name, style = titleStyle)
                        Text(
                            text = "${formatDate(it.t.start)} ${stringResource(MR.strings.to_)} ${
                                formatDate(it.t.end)
                            }, ${it.games.size} ${
                                stringResource(MR.plurals.games, it.games.size)
                            }",
                            style = detailsStyle,
                        )
                    }
                    IconButton({
                        setCurrent(it.t.id)
                        navigator.push(Screen.TournamentEditor)
                    }) {
                        Icon(Icons.Default.Edit, stringResource(MR.strings.edit_tournament))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        } else {
            item {
                Text(
                    text = stringResource(MR.strings.add_first_tournament_hint),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(normalPadding),
                )
            }
        }
    }
}
