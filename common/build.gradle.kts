plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("kotlin-parcelize")
    id("app.cash.sqldelight")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvmToolchain((property("tournamentscompose.versions.java") as String).toInt())
    androidTarget()
    jvm()
    sourceSets {
        val decomposeVersion = "3.0.0-alpha06"
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) implementation(
                    compose.components.resources
                )
                implementation("com.google.code.gson:gson:2.10.1")

                implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
                implementation("com.arkivanov.decompose:extensions-compose:$decomposeVersion")

                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform("androidx.compose:compose-bom:2024.02.00"))
                implementation("androidx.compose.ui:ui")
                //implementation("androidx.compose.ui:ui-tooling-preview")
                implementation("androidx.compose.runtime:runtime-livedata")
                implementation("androidx.compose.material3:material3")

                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                implementation("androidx.navigation:navigation-compose:2.7.7")

                implementation("app.cash.sqldelight:android-driver:2.0.1")
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)
                val multiplatformSettingsVersion = "1.1.1"
                implementation("com.russhwolf:multiplatform-settings:$multiplatformSettingsVersion")
                implementation("com.russhwolf:multiplatform-settings-coroutines:$multiplatformSettingsVersion")

                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            }
        }
    }
}

android {
    namespace = "me.frauenfelderflorian.tournamentscompose.common"
    compileSdk = (property("tournamentscompose.android.compileTargetSdk") as String).toInt()
    defaultConfig {
        minSdk = (property("tournamentscompose.android.minSdk") as String).toInt()
    }
    kotlin {
        jvmToolchain((property("tournamentscompose.versions.java") as String).toInt())
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("TournamentsDB") {
            packageName.set("me.frauenfelderflorian.tournamentscompose")
        }
    }
}
