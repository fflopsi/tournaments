package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.StackNavigation
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentWithGames

@Composable
expect fun TournamentList(
    navigator: StackNavigation<Screen>,
    tournaments: Map<UUID, TournamentWithGames>,
    setCurrent: (UUID?) -> Unit,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
)
