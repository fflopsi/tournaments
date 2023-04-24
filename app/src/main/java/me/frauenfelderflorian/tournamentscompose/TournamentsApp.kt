package me.frauenfelderflorian.tournamentscompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.data.Prefs
import me.frauenfelderflorian.tournamentscompose.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.data.TournamentsDatabase
import me.frauenfelderflorian.tournamentscompose.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.ui.AppSettings
import me.frauenfelderflorian.tournamentscompose.ui.GameEditor
import me.frauenfelderflorian.tournamentscompose.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.ui.PlayersEditor
import me.frauenfelderflorian.tournamentscompose.ui.TournamentEditor
import me.frauenfelderflorian.tournamentscompose.ui.TournamentList
import me.frauenfelderflorian.tournamentscompose.ui.TournamentViewer
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

class TournamentsAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { TournamentsApp(intent) }
    }
}

enum class Routes(val route: String) {
    TOURNAMENT_LIST("tl"),
    TOURNAMENT_EDITOR("te"),
    TOURNAMENT_VIEWER("tv"),
    GAME_EDITOR("ge"),
    GAME_VIEWER("gv"),
    PLAYERS_EDITOR("pe"),
    SETTINGS_EDITOR("se"),
}

@Composable
fun TournamentsApp(intent: Intent) {
    val context = LocalContext.current
    val prefs: Prefs = viewModel<Prefs>(factory = PrefsFactory(context)).apply { Initialize() }
    val db = Room.databaseBuilder(context, TournamentsDatabase::class.java, "tournaments").build()
    val tournamentDao = db.tournamentDao()
    val gameDao = db.gameDao()
    val model: TournamentsModel = viewModel()
    model.tournaments = tournamentDao.getTournamentsWithGames()
        .observeAsState(model.tournaments.values).value.associateBy { it.t.id }
    val navController = rememberNavController()

    TournamentsTheme(
        darkTheme = when (prefs.theme) {
            1 -> false
            2 -> true
            else -> isSystemInDarkTheme()
        },
        dynamicColor = prefs.dynamicColor,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.TOURNAMENT_LIST.route,
        ) {
            composable(Routes.TOURNAMENT_LIST.route) {
                TournamentList(
                    navController = navController,
                    tournaments = model.tournaments,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournamentDao = tournamentDao,
                    gameDao = gameDao,
                    intent = intent,
                )
            }
            composable(Routes.TOURNAMENT_EDITOR.route) {
                TournamentEditor(
                    navController = navController,
                    tournament = model.tournaments[model.current],
                    current = model.current,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournaments = model.tournaments,
                    dao = tournamentDao,
                    gameDao = gameDao,
                    defaultPlayers = prefs.players.toList(),
                    defaultAdaptivePoints = prefs.adaptivePoints,
                    defaultFirstPoints = prefs.firstPoints,
                    experimentalFeatures = prefs.experimentalFeatures,
                )
            }
            composable(Routes.TOURNAMENT_VIEWER.route) {
                TournamentViewer(
                    navController = navController,
                    tournament = model.tournaments[model.current]!!,
                )
            }
            composable(Routes.GAME_EDITOR.route) {
                GameEditor(
                    navController = navController,
                    tournament = model.tournaments[model.current]!!,
                    dao = gameDao,
                )
            }
            composable(Routes.GAME_VIEWER.route) {
                GameViewer(
                    navController = navController,
                    game = model.tournaments[model.current]!!.current!!,
                )
            }
            composable(
                route = "${Routes.PLAYERS_EDITOR.route}?players={players}",
                arguments = listOf(navArgument("players") {
                    defaultValue = "${context.resources.getString(R.string.player)} 1;${
                        context.resources.getString(R.string.player)
                    } 2"
                }),
            ) {
                PlayersEditor(
                    navController = navController,
                    formerPlayers = it.arguments?.getString("players"),
                )
            }
            composable(Routes.SETTINGS_EDITOR.route) {
                AppSettings(
                    navController = navController,
                    theme = prefs.theme,
                    updateTheme = prefs::saveTheme,
                    dynamicColor = prefs.dynamicColor,
                    updateDynamicColor = prefs::saveDynamicColor,
                    experimentalFeatures = prefs.experimentalFeatures,
                    updateExperimentalFeatures = prefs::saveExperimentalFeatures,
                    formerPlayers = prefs.players,
                    formerAdaptivePoints = prefs.adaptivePoints,
                    formerFirstPoints = prefs.firstPoints,
                    savePrefs = prefs::saveSettings,
                )
            }
        }
    }
}
