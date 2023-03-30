package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import androidx.room.Room
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.frauenfelderflorian.tournamentscompose.data.AppDatabase
import me.frauenfelderflorian.tournamentscompose.data.Prefs
import me.frauenfelderflorian.tournamentscompose.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.data.TournamentContainer
import me.frauenfelderflorian.tournamentscompose.data.User
import me.frauenfelderflorian.tournamentscompose.ui.AppSettings
import me.frauenfelderflorian.tournamentscompose.ui.GameEditor
import me.frauenfelderflorian.tournamentscompose.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.ui.PlayersEditor
import me.frauenfelderflorian.tournamentscompose.ui.TournamentEditor
import me.frauenfelderflorian.tournamentscompose.ui.TournamentList
import me.frauenfelderflorian.tournamentscompose.ui.TournamentViewer
import me.frauenfelderflorian.tournamentscompose.ui.getUsers

class TournamentsAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { TournamentsApp() }
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TournamentsApp() {
    val container: TournamentContainer = viewModel()
    val context = LocalContext.current
    val prefs: Prefs = viewModel(factory = PrefsFactory(context))
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "testDb").build()
    val dao = db.userDao()
    var usersTest = listOf<User>()
    LaunchedEffect(Unit) {
        launch { prefs.themeFlow.collect { prefs.useTheme(it) } }
        launch { prefs.dynamicColorFlow.collect { prefs.useDynamicColor(it) } }
        launch { prefs.playersFlow.collect { prefs.useSettings(newPlayers = it.split(";")) } }
        launch { prefs.adaptivePointsFlow.collect { prefs.useSettings(newAdaptivePoints = it) } }
        launch { prefs.firstPointsFlow.collect { prefs.useSettings(newFirstPoints = it) } }
        launch { withContext(Dispatchers.IO) { getUsers(dao).collect { usersTest = it } } }
    }
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.TOURNAMENT_LIST.route,
    ) {
        composable(
            route = Routes.TOURNAMENT_LIST.route,
            exitTransition = {
                if (container.current == -1) {
                    null
                } else {
                    slideOutHorizontally(targetOffsetX = { width -> -width })
                }
            },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { width -> -width }) },
        ) {
            TournamentList(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
                tournaments = container.tournaments,
                setCurrent = container::updateCurrent,
            )
        }
        composable(
            route = Routes.TOURNAMENT_EDITOR.route,
            enterTransition = {
                if (container.current == -1) {
                    scaleIn(transformOrigin = TransformOrigin(0.9f, 0.95f))
                } else {
                    slideInHorizontally(initialOffsetX = { width -> width })
                }
            },
            exitTransition = { slideOutHorizontally(targetOffsetX = { width -> -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { width -> -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width -> width }) },
        ) {
            TournamentEditor(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
                tournaments = container.tournaments,
                current = container.current,
                defaultPlayers = prefs.players.toList(),
                defaultAdaptivePoints = prefs.adaptivePoints,
                defaultFirstPoints = prefs.firstPoints,
            )
        }
        composable(
            route = Routes.TOURNAMENT_VIEWER.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { width -> width }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { width -> -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { width -> -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width -> width }) },
        ) {
            TournamentViewer(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
                tournament = container.tournaments[container.current],
            )
        }
        composable(Routes.GAME_EDITOR.route) {
            GameEditor(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
                tournament = container.tournaments[container.current],
            )
        }
        composable(Routes.GAME_VIEWER.route) {
            GameViewer(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
                game = container.tournaments[container.current].games[container.tournaments[container.current].current],
            )
        }
        composable(
            route = "${Routes.PLAYERS_EDITOR.route}?players={players}",
            arguments = listOf(navArgument("players") { defaultValue = "Player 1;Player 2" }),
        ) {
            PlayersEditor(
                navController = navController,
                theme = prefs.theme,
                dynamicColor = prefs.dynamicColor,
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
                formerPlayers = prefs.players,
                formerAdaptivePoints = prefs.adaptivePoints,
                formerFirstPoints = prefs.firstPoints,
                savePrefs = prefs::saveSettings,
                userDao = dao,
                userTest = usersTest,
            )
        }
    }
}
