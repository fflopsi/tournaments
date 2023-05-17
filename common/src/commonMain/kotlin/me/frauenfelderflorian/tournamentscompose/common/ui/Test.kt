package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR

@Composable
fun TestComposable() {
    Text(stringResource(MR.strings.settings))
}
