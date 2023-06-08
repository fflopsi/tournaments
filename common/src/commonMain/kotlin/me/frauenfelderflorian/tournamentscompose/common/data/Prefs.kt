package me.frauenfelderflorian.tournamentscompose.common.data

import androidx.compose.runtime.Composable

expect class Prefs {
    var theme: Int
    var dynamicColor: Boolean
    var experimentalFeatures: Boolean
    var players: List<String>
    var adaptivePoints: Boolean
    var firstPoints: Int

    @Composable
    fun Initialize()
}
