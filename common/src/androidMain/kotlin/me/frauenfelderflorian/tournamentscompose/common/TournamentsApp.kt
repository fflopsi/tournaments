package me.frauenfelderflorian.tournamentscompose.common

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsDatabase
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.common.ui.AppSettings
import me.frauenfelderflorian.tournamentscompose.common.ui.GameEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.Navigator
import me.frauenfelderflorian.tournamentscompose.common.ui.PlayersEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.Routes
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentList
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.theme.TournamentsTheme
import java.util.UUID

@Composable
fun TournamentsApp(intent: Intent) {
    val context = LocalContext.current
    val prefs: Prefs = viewModel<Prefs>(factory = PrefsFactory(context)).apply { Initialize() }
    val db = Room.databaseBuilder(context, TournamentsDatabase::class.java, "tournaments").build()
    val tournamentDao = db.tournamentDao()
    val gameDao = db.gameDao()
    val model: TournamentsModel = viewModel()
    model.tournaments = tournamentDao.getTournamentsWithGames().asLiveData()
        .observeAsState(model.tournaments.values).value.associateBy { it.t.id }
    val navigator = Navigator(rememberNavController())
    val systemUiController = rememberSystemUiController()
    val darkIcons = !isSystemInDarkTheme()
    DisposableEffect(systemUiController, prefs.theme) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = when (prefs.theme) {
                1 -> true
                2 -> false
                else -> darkIcons
            },
        )
        onDispose {}
    }

    TournamentsTheme(
        darkTheme = when (prefs.theme) {
            1 -> false
            2 -> true
            else -> isSystemInDarkTheme()
        },
        dynamicColor = prefs.dynamicColor,
    ) {
        NavHost(
            navController = navigator.controller as NavHostController,
            startDestination = Routes.TOURNAMENT_LIST.route,
        ) {
            composable(Routes.TOURNAMENT_LIST.route) {
                TournamentList(
                    navigator = navigator,
                    tournaments = model.tournaments,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournamentDao = tournamentDao,
                    gameDao = gameDao,
                    intent = intent,
                )
            }
            composable(Routes.TOURNAMENT_EDITOR.route) {
                TournamentEditor(
                    navigator = navigator,
                    tournament = model.tournaments[model.current],
                    current = model.current,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournaments = model.tournaments,
                    dao = tournamentDao,
                    gameDao = gameDao,
                    prefs = prefs,
                )
            }
            composable(Routes.TOURNAMENT_VIEWER.route) {
                TournamentViewer(
                    navigator = navigator,
                    tournament = model.tournaments[model.current]!!,
                )
            }
            composable(Routes.GAME_EDITOR.route) {
                GameEditor(
                    navigator = navigator,
                    tournament = model.tournaments[model.current]!!,
                    dao = gameDao,
                )
            }
            composable(Routes.GAME_VIEWER.route) {
                GameViewer(
                    navigator = navigator,
                    game = model.tournaments[model.current]!!.current!!,
                )
            }
            composable(Routes.PLAYERS_EDITOR.route) {
                PlayersEditor(
                    navigator = navigator,
                )
            }
            composable(Routes.SETTINGS_EDITOR.route) {
                AppSettings(
                    navigator = navigator,
                    prefs = prefs,
                )
            }
        }
    }
}