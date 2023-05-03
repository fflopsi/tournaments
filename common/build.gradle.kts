plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    jvmToolchain(17)
    android()
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
        val androidMain by getting
//        val desktopMain by getting {
//            dependencies {
//                implementation(compose.desktop.common)
//            }
//        }
    }
}

android {
    namespace = "me.frauenfelderflorian.tournamentscompose.common"
    compileSdk = 33
}
