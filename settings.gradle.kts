pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val kotlinVersion = "1.8.20"
        val agpVersion = "8.0.1"
        val composeVersion = "1.4.0"

        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("android") version kotlinVersion

        id("com.android.application") version agpVersion
        id("com.android.library") version agpVersion

        id("org.jetbrains.compose") version composeVersion

        id("dev.icerock.mobile.multiplatform-resources") version "0.22.0"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "TournamentsCompose"
include(":androidApp", ":desktopApp", ":common")
