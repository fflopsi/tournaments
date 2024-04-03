// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("multiplatform") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.compose") apply false
    id("app.cash.sqldelight") version "2.0.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}
