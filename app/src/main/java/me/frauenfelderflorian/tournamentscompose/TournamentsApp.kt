package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
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

const val ROUTE_TOURNAMENT_LIST = "tl"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TournamentsApp() {
    val model: TournamentContainer = viewModel()
    val navController = rememberAnimatedNavController()
    val width = ((LocalConfiguration.current.densityDpi / 160f)
            * LocalConfiguration.current.screenWidthDp).roundToInt()
    AnimatedNavHost(
        navController = navController,
        startDestination = ROUTE_TOURNAMENT_LIST
    ) {
        composable(ROUTE_TOURNAMENT_LIST) { TournamentListScreen(navController, model.tournaments) }
        composable(
            ROUTE_TOURNAMENT_EDITOR,
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { width }) },
        ) { TournamentEditor(navController, model.tournaments) }
    }
}
