package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun InfoDialog(showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            // TODO: Move to commonMain when AlertDialog is M3
            // icon = { Icon(Icons.Default.Info, null) },
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
