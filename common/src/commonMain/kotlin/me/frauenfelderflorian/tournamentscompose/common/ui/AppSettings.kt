package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.StackNavigation
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs

@Composable
expect fun AppSettings(
    navigator: StackNavigation<Screen>,
    prefs: Prefs,
    playersModel: PlayersModel,
)