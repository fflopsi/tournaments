package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import java.text.DateFormat
import me.frauenfelderflorian.tournamentscompose.R

fun formatDate(date: Long): String = DateFormat.getDateInstance(DateFormat.SHORT).format(date)

@Composable
fun getTheme(theme: Int): Boolean {
    return when (theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
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
