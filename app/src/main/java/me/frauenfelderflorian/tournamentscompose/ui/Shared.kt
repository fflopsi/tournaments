package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import java.text.DateFormat
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes

fun formatDate(date: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(date)

@Composable
fun getTheme(theme: Int): Boolean {
    return when (theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
}

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
