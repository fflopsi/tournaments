package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.data.Prefs
import me.frauenfelderflorian.tournamentscompose.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.data.TournamentContainer
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

class TournamentsAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TournamentsApp() }
    }
}

enum class Routes(val route: String) {
    TOURNAMENT_LIST("tl"),
    TOURNAMENT_EDITOR("te"),
    TOURNAMENT_VIEWER("tv"),
    PLAYERS_EDITOR("pe"),
    SETTINGS_EDITOR("se"),
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TournamentsApp() {
    val container: TournamentContainer = viewModel()
    val prefs: Prefs = viewModel(factory = PrefsFactory(LocalContext.current))
    LaunchedEffect(Unit) {
        launch {
            prefs.themeFlow.collect { prefs.useTheme(it) }
        }
        launch {
            prefs.playersFlow.collect { prefs.useSettings(newPlayers = it.split(";")) }
        }
        launch {
            prefs.adaptivePointsFlow.collect { prefs.useSettings(newAdaptivePoints = it) }
        }
        launch {
            prefs.firstPointsFlow.collect { prefs.useSettings(newFirstPoints = it) }
        }
    }
    val navController = rememberAnimatedNavController()
    val width = ((LocalConfiguration.current.densityDpi / 160f)
            * LocalConfiguration.current.screenWidthDp).roundToInt() //check if necessary
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.TOURNAMENT_LIST.route
    ) {
        composable(
            route = Routes.TOURNAMENT_LIST.route,
            exitTransition = {
                if (container.current == -1) null
                else slideOutHorizontally(targetOffsetX = { -width })
            },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
        ) {
            TournamentList(
                navController = navController,
                theme = prefs.theme,
                tournaments = container.tournaments,
                setCurrent = container::updateCurrent
            )
        }
        composable(
            route = Routes.TOURNAMENT_EDITOR.route,
            enterTransition = {
                if (container.current == -1) scaleIn(transformOrigin = TransformOrigin(0.9f, 0.95f))
                else slideInHorizontally(initialOffsetX = { width })
            },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) }
        ) {
            TournamentEditor(
                navController = navController,
                theme = prefs.theme,
                tournaments = container.tournaments,
                current = container.current
            )
        }
        composable(
            route = Routes.TOURNAMENT_VIEWER.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) }
        ) {
            TournamentViewer(
                navController = navController,
                theme = prefs.theme,
                tournaments = container.tournaments,
                current = container.current
            )
        }
        composable(
            route = Routes.PLAYERS_EDITOR.route + "?players={players}",
            arguments = listOf(navArgument("players") { defaultValue = "Default Player" }),
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) },
        ) {
            PlayersEditor(
                navController = navController,
                theme = prefs.theme,
                formerPlayers = it.arguments?.getString("players")
            )
        }
        composable(
            route = Routes.SETTINGS_EDITOR.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) }, //TODO
            exitTransition = { slideOutHorizontally(targetOffsetX = { -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) }, //TODO
        ) {
            SettingsEditor(
                navController = navController,
                theme = prefs.theme,
                updateTheme = prefs::saveTheme,
                formerPlayers = prefs.players,
                formerAdaptivePoints = prefs.adaptivePoints,
                formerFirstPoints = prefs.firstPoints,
                savePrefs = prefs::saveSettings,
            )
        }
    }
}

fun formatDate(cal: GregorianCalendar): String =
    DateFormat.getDateInstance(DateFormat.SHORT).format(cal.time)

@Composable
fun getTheme(theme: Int): Boolean {
    return when (theme) {
        1 -> false
        2 -> true
        else -> {
            isSystemInDarkTheme()
        }
    }
}
