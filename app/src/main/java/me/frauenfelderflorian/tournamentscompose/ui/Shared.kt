package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.DateFormat
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes

fun formatDate(date: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(date)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarTitle(text: String, scrollBehavior: TopAppBarScrollBehavior) {
    Text(
        text = text,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (scrollBehavior.state.collapsedFraction < 0.5f) 2 else 1,
    )
}

@Composable
fun BackButton(navController: NavController) {
    IconButton({ navController.popBackStack() }) {
        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
    }
}

@Composable
fun InfoDialog(showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            icon = { Icon(Icons.Default.Info, null) },
            title = {
                Text("${stringResource(R.string.about)} ${stringResource(R.string.app_title)}")
            },
            text = { Text(stringResource(R.string.built_by_info)) },
            confirmButton = {
                TextButton({ showDialog.value = false }) { Text(stringResource(R.string.ok)) }
            },
        )
    }
}

@Composable
fun SettingsInfoMenu(navController: NavController, showInfoDialog: MutableState<Boolean>) {
    Box {
        var expanded by remember { mutableStateOf(false) }

        IconButton({ expanded = true }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.more_actions))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings)) },
                onClick = {
                    expanded = false
                    navController.navigate(Routes.SETTINGS_EDITOR.route)
                },
                leadingIcon = { Icon(Icons.Default.Settings, null) },
            )
            Divider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about)) },
                onClick = {
                    expanded = false
                    showInfoDialog.value = true
                },
                leadingIcon = { Icon(Icons.Outlined.Info, null) },
            )
        }
    }
}

@Composable
fun PlayersSetting(navController: NavController, players: List<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${stringResource(R.string.players)}: ${players.joinToString(", ")}",
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically),
        )
        IconButton({
            navController.navigate(
                "${Routes.PLAYERS_EDITOR.route}${
                    if (players.isNotEmpty()) "?players=${players.joinToString(";")}" else ""
                }"
            )
        }) {
            Icon(Icons.Default.Edit, stringResource(R.string.edit_players))
        }
    }
}

@Composable
fun TournamentCreationSettings(
    adaptivePoints: MutableState<Boolean>,
    onClickAdaptivePoints: () -> Unit,
    firstPointsString: MutableState<Int?>,
    onChangeFirstPoints: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClickAdaptivePoints),
        ) {
            Column(
                Modifier
                    .weight(2f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    "${stringResource(R.string.point_system)}: ${
                        stringResource(
                            if (adaptivePoints.value) R.string.adaptive else R.string.classic
                        )
                    }"

                )
                Text(
                    text = stringResource(
                        if (adaptivePoints.value) {
                            R.string.point_system_adaptive
                        } else {
                            R.string.point_system_classic
                        }
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                )
            }
            Switch(checked = adaptivePoints.value, onCheckedChange = null)
        }
        AnimatedVisibility(
            visible = adaptivePoints.value,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Text(
                text = stringResource(R.string.point_system_adaptive_desc),
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
            )
        }
        AnimatedVisibility(
            visible = !adaptivePoints.value,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column {
                TextField(
                    value = firstPointsString.value.toString(),
                    onValueChange = onChangeFirstPoints,
                    singleLine = true,
                    label = { Text(stringResource(R.string.first_points)) },
                    trailingIcon = { Icon(Icons.Default.Star, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.point_system_classic_desc),
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() }),
    ) {
        elements.toList().toMutableStateList()
    }
}

@Composable
fun rememberMutableStateMapOf(vararg elements: Pair<Int, String>): SnapshotStateMap<Int, String> {
    return rememberSaveable(
        saver = listSaver(
            save = { map -> map.toList().map { "${it.first};${it.second}" } },
            restore = { list ->
                list.map { it.substringBefore(";").toInt() to it.substringAfter(";") }
                    .toMutableStateMap()
            },
        ),
    ) {
        elements.toList().map { it.first to it.second }.toMutableStateMap()
    }
}
