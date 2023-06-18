package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.StackNavigation
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames

@Composable
expect fun TournamentViewer(
    navigator: StackNavigation<Screen>,
    tournament: TournamentWithGames,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
)
