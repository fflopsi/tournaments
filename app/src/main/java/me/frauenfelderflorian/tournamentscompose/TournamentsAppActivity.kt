package me.frauenfelderflorian.tournamentscompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.data.TournamentContainer
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsComposeTheme
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
        composable(ROUTE_TOURNAMENT_LIST) { TournamentList(navController, model.tournaments) }
        composable(
            ROUTE_TOURNAMENT_EDITOR,
            enterTransition = { slideInHorizontally(initialOffsetX = { width }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { width }) },
        ) { TournamentEditor(navController, model.tournaments) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(navController: NavHostController, tournaments: MutableList<Tournament>) {
    TournamentsComposeTheme {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(text = stringResource(R.string.app_title)) },
                    actions = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.about))
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                val context = LocalContext.current
                FloatingActionButton(onClick = {
                    Toast.makeText(
                        context,
                        "This will add a new tournament",
                        Toast.LENGTH_SHORT,
                    ).show()
                    navController.navigate(ROUTE_TOURNAMENT_EDITOR)
                }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add_new_tournament))
                }
            }
        ) { paddingValues: PaddingValues ->
            TournamentList(tournaments, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun TournamentList(tournaments: MutableList<Tournament>, modifier: Modifier) {
    LazyColumn(modifier.fillMaxHeight()) {
        items(items = tournaments, itemContent = { item ->
            Text(text = item.name, modifier = Modifier.padding(8.dp))
        })
    }
}
