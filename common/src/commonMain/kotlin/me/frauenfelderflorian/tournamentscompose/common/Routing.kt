package me.frauenfelderflorian.tournamentscompose.common

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.StackNavigation
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.data.GameDao
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentDao
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.common.ui.AppSettings
import me.frauenfelderflorian.tournamentscompose.common.ui.ChildStack
import me.frauenfelderflorian.tournamentscompose.common.ui.GameEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.PlayerViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.PlayersEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.Screen
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentList
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentViewer

@Composable
fun TournamentStack(
    navigator: StackNavigation<Screen>,
    prefs: Prefs,
    tournamentsModel: TournamentsModel,
    tournamentDao: TournamentDao,
    gameDao: GameDao,
    playersModel: PlayersModel,
) {
    ChildStack(
        source = navigator,
        initialStack = { listOf(Screen.TournamentList) },
        handleBackButton = true,
    ) {
        when (it) {
            is Screen.TournamentList -> TournamentList(
                navigator = navigator,
                tournaments = tournamentsModel.tournaments,
                setCurrent = { new: UUID? -> tournamentsModel.current = new },
                tournamentDao = tournamentDao,
                gameDao = gameDao,
            )

            is Screen.TournamentEditor -> TournamentEditor(
                navigator = navigator,
                tournament = tournamentsModel.tournaments[tournamentsModel.current],
                current = tournamentsModel.current,
                setCurrent = { new: UUID? -> tournamentsModel.current = new },
                tournaments = tournamentsModel.tournaments,
                dao = tournamentDao,
                gameDao = gameDao,
                prefs = prefs,
                playersModel = playersModel,
            )

            is Screen.TournamentViewer -> TournamentViewer(
                navigator = navigator,
                tournament = tournamentsModel.tournaments[tournamentsModel.current]!!,
                tournamentDao = tournamentDao,
                gameDao = gameDao,
            )

            is Screen.PlayerViewer -> PlayerViewer(
                navigator = navigator,
                tournament = tournamentsModel.tournaments[tournamentsModel.current]!!,
                player = it.player,
            )

            is Screen.GameEditor -> GameEditor(
                navigator = navigator,
                tournament = tournamentsModel.tournaments[tournamentsModel.current]!!,
                dao = gameDao,
            )

            is Screen.GameViewer -> GameViewer(
                navigator = navigator,
                game = tournamentsModel.tournaments[tournamentsModel.current]!!.current!!,
            )

            is Screen.PlayersEditor -> PlayersEditor(
                navigator = navigator,
                playersModel = playersModel,
            )

            is Screen.AppSettings -> AppSettings(
                navigator = navigator,
                prefs = prefs,
                playersModel = playersModel,
            )
        }
    }
}
