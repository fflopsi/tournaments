package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.frauenfelderflorian.tournamentscompose.data.TournamentContainer
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
    PLAYERS_EDITOR("pe")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TournamentsApp() {
    val model: TournamentContainer = viewModel()
    val navController = rememberAnimatedNavController()
    val width = ((LocalConfiguration.current.densityDpi / 160f)
            * LocalConfiguration.current.screenWidthDp).roundToInt()
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.TOURNAMENT_LIST.route
    ) {
        composable(
            route = Routes.TOURNAMENT_LIST.route,
            exitTransition = {
                if (model.current == -1) null
                else slideOutHorizontally(targetOffsetX = { -width })
            },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
        ) { TournamentListScreen(navController, model.tournaments) }
        composable(
            route = Routes.TOURNAMENT_EDITOR.route,
            enterTransition = {
                if (model.current == -1) scaleIn(transformOrigin = TransformOrigin(0.9f, 0.95f))
                else slideInHorizontally(initialOffsetX = { width })
            },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) }
        ) { TournamentEditor(navController, model.tournaments) }
        composable(
            route = Routes.TOURNAMENT_VIEWER.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -width }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) }
        ) {
            TournamentViewer(
                navController = navController,
                tournaments = model.tournaments,
                current = model.current
            )
        }
        composable(
            route = Routes.PLAYERS_EDITOR.route + "?players={players}",
            arguments = listOf(navArgument("players") { defaultValue = "Default Player" }),
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { width }) },
        ) { backStackEntry ->
            PlayersEditor(navController, backStackEntry.arguments?.getString("players"))
        }
    }
}
