package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
actual val insets: WindowInsets
    @Composable
    get() = ScaffoldDefaults.contentWindowInsets
