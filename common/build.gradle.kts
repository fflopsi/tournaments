plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    android()
    jvm()
    sourceSets {
        val commonMain by getting
        val androidMain by getting
//        val desktopMain by getting {
//            dependencies {
//                implementation(compose.desktop.common)
//            }
//        }
    }
}

android {
    namespace = "me.frauenfelderflorian.tournamentscompose"
    compileSdk = 33
}
