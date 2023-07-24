package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

actual val insets: WindowInsets
    @Composable
    get() = ScaffoldDefaults.contentWindowInsets
