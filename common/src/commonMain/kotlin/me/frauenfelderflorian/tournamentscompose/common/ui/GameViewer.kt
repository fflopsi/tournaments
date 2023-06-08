package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import dev.icerock.moko.resources.compose.stringResource
import me.frauenfelderflorian.tournamentscompose.common.MR
import me.frauenfelderflorian.tournamentscompose.common.data.Game

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameViewer(
    navigator: StackNavigation<Screen>,
    game: Game,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    TopAppBarTitle(
                        text = stringResource(MR.strings.game_title, formatDate(game.date)),
                        scrollBehavior = scrollBehavior,
                    )
                },
                navigationIcon = { BackButton { navigator.pop() } },
                actions = {
                    IconButton({ navigator.push(Screen.GameEditor) }) {
                        Icon(Icons.Default.Edit, stringResource(MR.strings.edit_game))
                    }
                    SettingsInfoMenu(
                        navigateToSettings = {
                            navigator.push(Screen.AppSettings)
                        },
                        showInfoDialog = showInfo,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = insets,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        GameViewerContent(game = game, modifier = Modifier.padding(paddingValues))
        InfoDialog(showInfo)
    }
}
