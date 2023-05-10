plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("dev.icerock.mobile.multiplatform-resources")
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
                implementation("dev.icerock.moko:resources:0.22.0")
                implementation("dev.icerock.moko:resources-compose:0.22.0")
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

multiplatformResources {
    multiplatformResourcesPackage = "me.frauenfelderflorian.tournamentscompose.common"
}

android {
    namespace = "me.frauenfelderflorian.tournamentscompose.common"
    compileSdk = 33
    kotlin {
        jvmToolchain(17)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
