package me.frauenfelderflorian.tournamentscompose.common.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun TournamentsTheme(
    darkTheme: Boolean,
    /**
     * Not implemented
     */
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
