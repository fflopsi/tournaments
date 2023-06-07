package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

actual val insets: WindowInsets
    @Composable
    get() = WindowInsets.ime.union(WindowInsets.systemBars)
