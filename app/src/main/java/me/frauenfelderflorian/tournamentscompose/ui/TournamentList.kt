package me.frauenfelderflorian.tournamentscompose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.frauenfelderflorian.tournamentscompose.R
import me.frauenfelderflorian.tournamentscompose.Routes
import me.frauenfelderflorian.tournamentscompose.data.Tournament
import me.frauenfelderflorian.tournamentscompose.ui.theme.TournamentsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentList(
    navController: NavHostController,
    theme: Int,
    dynamicColor: Boolean,
    tournaments: MutableList<Tournament>,
    setCurrent: (Int) -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val showInfo = rememberSaveable { mutableStateOf(false) }

    TournamentsTheme(darkTheme = getTheme(theme), dynamicColor = dynamicColor) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.app_title)) },
                    actions = {
                        IconButton({ navController.navigate(Routes.SETTINGS_EDITOR.route) }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                        }
                        IconButton({ showInfo.value = true }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.about))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.new_tournament)) },
                    expanded = scrollBehavior.state.collapsedFraction < 0.5f,
                    onClick = {
                        setCurrent(-1)
                        navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                    },
                )
            },
            contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues),
            ) {
                if (tournaments.isNotEmpty()) {
                    items(tournaments.sortedByDescending { it.start }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.clickable {
                                setCurrent(tournaments.indexOf(it))
                                navController.navigate(Routes.TOURNAMENT_VIEWER.route)
                            },
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.tournament_list_title,
                                    it.name,
                                    formatDate(it.start),
                                    formatDate(it.end),
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f),
                            )
                            IconButton({
                                setCurrent(tournaments.indexOf(it))
                                navController.navigate(Routes.TOURNAMENT_EDITOR.route)
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.edit_tournament))
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.add_first_tournament_hint),
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.ExtraLight,
                        )
                    }
                }
            }
            InfoDialog(showDialog = showInfo)
        }
    }
}
