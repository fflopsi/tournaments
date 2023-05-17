package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import java.text.DateFormat

val titleStyle @Composable get() = MaterialTheme.typography.titleLarge
val detailsStyle @Composable get() = MaterialTheme.typography.bodyMedium
val normalDp = 16.dp
val normalPadding = PaddingValues(normalDp, normalDp)
val gson: Gson = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()

fun formatDate(date: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(date)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarTitle(text: String, scrollBehavior: TopAppBarScrollBehavior) {
    Text(
        text = text,
        overflow = TextOverflow.Ellipsis,
        maxLines = if (scrollBehavior.state.collapsedFraction < 0.5f) 2 else 1,
        style = if (scrollBehavior.state.collapsedFraction < 0.5f) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.headlineSmall
        },
    )
}

@Composable
fun BackButton(navigateUp: () -> Unit) {
    IconButton(navigateUp) {
        Icon(Icons.Default.ArrowBack, stringResource(MR.strings.back))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InfoDialog(showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            //icon = { Icon(Icons.Default.Info, null) }, // TODO: uncomment when AlertDialog is M3
            title = {
                Text("${stringResource(MR.strings.about)} ${stringResource(MR.strings.app_title)}")
            },
            text = {
                Column {
                    Text(stringResource(MR.strings.built_by_info))
                    val tag = stringResource(MR.strings.github_link_tag)
                    val linkString = buildAnnotatedString {
                        val string = stringResource(MR.strings.link_to_github)
                        append(string)
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline,
                            ),
                            start = 0,
                            end = string.length,
                        )
                        addStringAnnotation(
                            tag = tag,
                            annotation = stringResource(MR.strings.github_link),
                            start = 0,
                            end = string.length,
                        )
                    }
                    val uriHandler = LocalUriHandler.current
                    ClickableText(
                        text = linkString,
                        onClick = { pos ->
                            linkString.getStringAnnotations(tag, pos, pos).firstOrNull()
                                ?.let { uriHandler.openUri(it.item) }
                        },
                    )
                }
            },
            confirmButton = {
                TextButton({ showDialog.value = false }) { Text(stringResource(MR.strings.ok)) }
            },
        )
    }
}

@Composable
fun SettingsInfoMenu(navigateToSettings: () -> Unit, showInfoDialog: MutableState<Boolean>) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        IconButton({ expanded = true }) {
            Icon(Icons.Default.MoreVert, stringResource(MR.strings.more_actions))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.settings)) },
                onClick = {
                    expanded = false
                    navigateToSettings()
                },
                leadingIcon = { Icon(Icons.Default.Settings, null) },
            )
            Divider()
            DropdownMenuItem(
                text = { Text(stringResource(MR.strings.about)) },
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
fun PlayersSetting(players: List<String>, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick).padding(normalPadding),
    ) {
        Column(Modifier.weight(2f)) {
            Text(
                text = stringResource(MR.strings.players),
                style = titleStyle,
            )
            Text(
                text = if (players.isNotEmpty()) {
                    players.joinToString(", ")
                } else {
                    stringResource(MR.strings.no_players)
                },
                style = detailsStyle,
                fontStyle = if (players.isNotEmpty()) FontStyle.Normal else FontStyle.Italic,
            )
        }
        Icon(Icons.Default.Edit, stringResource(MR.strings.edit_players))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointSystemSettings(
    adaptivePoints: Boolean,
    onClickAdaptivePoints: () -> Unit,
    firstPoints: Int?,
    onChangeFirstPoints: (String) -> Unit,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClickAdaptivePoints).padding(normalPadding),
        ) {
            Column(Modifier.weight(2f)) {
                Text(text = stringResource(MR.strings.adaptive_point_system), style = titleStyle)
                Text(
                    text = stringResource(
                        if (adaptivePoints) {
                            MR.strings.point_system_adaptive_desc
                        } else {
                            MR.strings.point_system_classic_desc
                        }
                    ),
                    style = detailsStyle,
                )
            }
            Switch(checked = adaptivePoints, onCheckedChange = null)
        }
        AnimatedVisibility(
            visible = !adaptivePoints,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            OutlinedTextField(
                value = firstPoints?.toString() ?: "",
                onValueChange = onChangeFirstPoints,
                singleLine = true,
                label = { Text(stringResource(MR.strings.first_points)) },
                trailingIcon = { Icon(Icons.Default.Edit, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(normalPadding),
            )
        }
        Text(
            text = stringResource(
                if (adaptivePoints) {
                    MR.strings.point_system_adaptive_info
                } else {
                    MR.strings.point_system_classic_info
                }
            ),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(normalPadding).animateContentSize(),
        )
        Text(
            text = stringResource(
                if (adaptivePoints) {
                    MR.strings.point_system_adaptive_expl
                } else {
                    MR.strings.point_system_classic_expl
                }
            ),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(normalPadding).animateContentSize(),
        )
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
