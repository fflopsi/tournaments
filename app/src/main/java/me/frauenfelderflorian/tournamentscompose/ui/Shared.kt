package me.frauenfelderflorian.tournamentscompose.ui

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.text.DateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.GameDao
import me.frauenfelderflorian.tournamentscompose.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.data.TournamentWithGames

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
            text = {
                Column {
                    Text(stringResource(R.string.built_by_info))
                    val tag = stringResource(R.string.github_link_tag)
                    val linkString = buildAnnotatedString {
                        val string = stringResource(R.string.link_to_github)
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
                            annotation = stringResource(R.string.github_link),
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
        modifier = Modifier.padding(normalPadding),
    ) {
        Column(Modifier.weight(2f)) {
            Text(
                text = stringResource(R.string.players),
                style = titleStyle,
            )
            Text(
                text = if (players.isNotEmpty()) {
                    players.joinToString(", ")
                } else {
                    stringResource(R.string.no_players)
                },
                style = detailsStyle,
                fontStyle = if (players.isNotEmpty()) FontStyle.Normal else FontStyle.Italic,
            )
        }
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
fun PointSystemSettings(
    adaptivePoints: MutableState<Boolean>,
    onClickAdaptivePoints: () -> Unit,
    firstPoints: MutableState<Int?>,
    onChangeFirstPoints: (String) -> Unit,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onClickAdaptivePoints)
                .padding(normalPadding),
        ) {
            Column(Modifier.weight(2f)) {
                Text(text = stringResource(R.string.adaptive_point_system), style = titleStyle)
                Text(
                    text = stringResource(
                        if (adaptivePoints.value) {
                            R.string.point_system_adaptive_desc
                        } else {
                            R.string.point_system_classic_desc
                        }
                    ),
                    style = detailsStyle,
                )
            }
            Switch(checked = adaptivePoints.value, onCheckedChange = null)
        }
        AnimatedVisibility(
            visible = !adaptivePoints.value,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            OutlinedTextField(
                value = firstPoints.value?.toString() ?: "",
                onValueChange = onChangeFirstPoints,
                singleLine = true,
                label = { Text(stringResource(R.string.first_points)) },
                trailingIcon = { Icon(Icons.Default.Edit, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(normalPadding),
            )
        }
        Text(
            text = stringResource(
                if (adaptivePoints.value) {
                    R.string.point_system_adaptive_info
                } else {
                    R.string.point_system_classic_info
                }
            ),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .padding(normalPadding)
                .animateContentSize(),
        )
        Text(
            text = stringResource(
                if (adaptivePoints.value) {
                    R.string.point_system_adaptive_expl
                } else {
                    R.string.point_system_classic_expl
                }
            ),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .padding(normalPadding)
                .animateContentSize(),
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

fun exportToUri(
    uri: Uri?,
    context: Context,
    scope: CoroutineScope,
    hostState: SnackbarHostState,
    content: Any,
) {
    try {
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(gson.toJson(content, content::class.java).toByteArray())
                it.close()
            }
        } else {
            scope.launch { hostState.showSnackbar(context.getString(R.string.exception_file)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                context.getString(
                    when (e) {
                        is FileNotFoundException -> R.string.exception_file
                        is IOException -> R.string.exception_io
                        else -> R.string.exception
                    }
                )
            )
        }
    }

}

fun importFromUri(
    uri: Uri?,
    context: Context,
    scope: CoroutineScope,
    hostState: SnackbarHostState,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
) {
    try {
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                gson.fromJson<Collection<TournamentWithGames>>(
                    BufferedReader(InputStreamReader(inputStream)).readText(),
                    object : TypeToken<Collection<TournamentWithGames>>() {}.type,
                ).forEach {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            tournamentDao.upsert(it.t)
                            gameDao.upsert(*it.games.toTypedArray())
                        }
                    }
                }
                inputStream.close()
            }
        } else {
            scope.launch { hostState.showSnackbar(context.getString(R.string.exception_file)) }
        }
    } catch (e: java.lang.Exception) {
        scope.launch {
            hostState.showSnackbar(
                context.getString(
                    when (e) {
                        is FileNotFoundException -> R.string.exception_file
                        is JsonSyntaxException -> R.string.exception_json
                        is IOException -> R.string.exception_io
                        else -> R.string.exception
                    }
                )
            )
        }
    }
}
