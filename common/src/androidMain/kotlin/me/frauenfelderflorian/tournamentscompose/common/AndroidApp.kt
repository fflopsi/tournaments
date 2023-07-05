package me.frauenfelderflorian.tournamentscompose.common

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.icerock.moko.resources.compose.stringResource
import java.util.UUID
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsDatabase
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.common.ui.AppSettings
import me.frauenfelderflorian.tournamentscompose.common.ui.ChildStack
import me.frauenfelderflorian.tournamentscompose.common.ui.GameEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.GameViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.PlayerViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.PlayersEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.ProvideComponentContext
import me.frauenfelderflorian.tournamentscompose.common.ui.Screen
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentEditor
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentList
import me.frauenfelderflorian.tournamentscompose.common.ui.TournamentViewer
import me.frauenfelderflorian.tournamentscompose.common.ui.importFromUri
import me.frauenfelderflorian.tournamentscompose.common.ui.theme.TournamentsTheme

fun androidApp(activity: ComponentActivity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    val rootComponentContext = activity.defaultComponentContext()
    activity.setContent {
        ProvideComponentContext(rootComponentContext) { AndroidAppContent(activity.intent) }
    }
}

@Composable
fun AndroidAppContent(intent: Intent) {
    val context = LocalContext.current
    val prefs: Prefs = viewModel<Prefs>(factory = PrefsFactory(context)).apply { Initialize() }
    val db = Room.databaseBuilder(context, TournamentsDatabase::class.java, "tournaments").build()
    val tournamentDao = db.tournamentDao()
    val gameDao = db.gameDao()
    val model: TournamentsModel = viewModel()
    model.tournaments = tournamentDao.getTournamentsWithGames().asLiveData()
        .observeAsState(model.tournaments.values).value.associateBy { it.t.id }
    val playersModel: PlayersModel = viewModel()
    val navigator = remember { StackNavigation<Screen>() }
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
        val scope = rememberCoroutineScope()
        var showImport by rememberSaveable { mutableStateOf(false) }
        var showedImport by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (!showedImport && intent.data != null && intent.action == Intent.ACTION_VIEW && intent.data!!.scheme == "content") {
                showImport = true
            }
        }
        if (showImport) {
            AlertDialog(
                onDismissRequest = {
                    showImport = false
                    showedImport = true
                },
                icon = { Icon(Icons.Default.ArrowDownward, null) },
                title = { Text(stringResource(MR.strings.import_)) },
                text = { Text(stringResource(MR.strings.import_info)) },
                confirmButton = {
                    TextButton({
                        showImport = false
                        importFromUri(
                            uri = intent.data,
                            context = context,
                            scope = scope,
                            tournamentDao = tournamentDao,
                            gameDao = gameDao,
                        )
                        showedImport = true
                    }) {
                        Text(stringResource(MR.strings.ok))
                    }
                },
                dismissButton = {
                    TextButton({
                        showImport = false
                        showedImport = true
                    }) {
                        Text(stringResource(MR.strings.cancel))
                    }
                },
            )
        }

        ChildStack(
            source = navigator,
            initialStack = { listOf(Screen.TournamentList) },
            handleBackButton = true,
        ) {
            when (it) {
                is Screen.TournamentList -> TournamentList(
                    navigator = navigator,
                    tournaments = model.tournaments,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournamentDao = tournamentDao,
                    gameDao = gameDao,
                )

                is Screen.TournamentEditor -> TournamentEditor(
                    navigator = navigator,
                    tournament = model.tournaments[model.current],
                    current = model.current,
                    setCurrent = { new: UUID? -> model.current = new },
                    tournaments = model.tournaments,
                    dao = tournamentDao,
                    gameDao = gameDao,
                    prefs = prefs,
                    playersModel = playersModel,
                )

                is Screen.TournamentViewer -> TournamentViewer(
                    navigator = navigator,
                    tournament = model.tournaments[model.current]!!,
                    tournamentDao = tournamentDao,
                    gameDao = gameDao,
                )

                is Screen.PlayerViewer -> PlayerViewer(
                    navigator = navigator,
                    tournament = model.tournaments[model.current]!!,
                    player = it.player,
                )

                is Screen.GameEditor -> GameEditor(
                    navigator = navigator,
                    tournament = model.tournaments[model.current]!!,
                    dao = gameDao,
                )

                is Screen.GameViewer -> GameViewer(
                    navigator = navigator,
                    game = model.tournaments[model.current]!!.current!!,
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
}