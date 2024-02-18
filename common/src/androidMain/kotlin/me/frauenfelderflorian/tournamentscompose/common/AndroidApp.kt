package me.frauenfelderflorian.tournamentscompose.common

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import kotlinx.coroutines.launch
import me.frauenfelderflorian.tournamentscompose.common.data.PlayersModel
import me.frauenfelderflorian.tournamentscompose.common.data.Prefs
import me.frauenfelderflorian.tournamentscompose.common.data.PrefsFactory
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsDatabase
import me.frauenfelderflorian.tournamentscompose.common.data.TournamentsModel
import me.frauenfelderflorian.tournamentscompose.common.ui.ProvideComponentContext
import me.frauenfelderflorian.tournamentscompose.common.ui.Screen
import me.frauenfelderflorian.tournamentscompose.common.ui.importFromUri
import me.frauenfelderflorian.tournamentscompose.common.ui.theme.TournamentsTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import tournamentscompose.common.generated.resources.*

fun androidApp(activity: ComponentActivity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    val rootComponentContext = activity.defaultComponentContext()
    activity.enableEdgeToEdge()
    activity.setContent {
        ProvideComponentContext(rootComponentContext) { AndroidAppContent(activity.intent) }
    }
}

@OptIn(ExperimentalResourceApi::class)
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
                title = { Text(stringResource(Res.string.import)) },
                text = { Text(stringResource(Res.string.import_info)) },
                confirmButton = {
                    TextButton({
                        showImport = false
                        scope.launch {
                            importFromUri(
                                uri = intent.data,
                                context = context,
                                scope = scope,
                                tournamentDao = tournamentDao,
                                gameDao = gameDao,
                            )
                        }
                        showedImport = true
                    }) {
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = {
                    TextButton({
                        showImport = false
                        showedImport = true
                    }) {
                        Text(stringResource(Res.string.cancel))
                    }
                },
            )
        }
        TournamentStack(
            navigator = navigator,
            prefs = prefs,
            tournamentsModel = model,
            tournamentDao = tournamentDao,
            gameDao = gameDao,
            playersModel = playersModel,
        )
    }
}