plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("kotlin-parcelize")
    id("androidx.room")
}

kotlin {
    jvmToolchain((property("tournamentscompose.versions.java") as String).toInt())
    androidTarget()
    jvm()
    sourceSets {
        val decomposeVersion = "2.2.2"
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) implementation(
                    compose.components.resources
                )
                implementation("com.google.code.gson:gson:2.10.1")
                implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
                implementation("com.arkivanov.decompose:extensions-compose-jetbrains:$decomposeVersion")
            }
        }
        val androidMain by getting {
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

                val roomVersion = "2.6.1"
                implementation("androidx.room:room-runtime:$roomVersion")
                implementation("androidx.room:room-ktx:$roomVersion")
                //annotationProcessor("androidx.room:room-compiler:$roomVersion")
                //configurations["kapt"].dependencies.add(project.dependencies.create("androidx.room:room-compiler:$roomVersion"))

                implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
//                implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
//                implementation("com.arkivanov.decompose:extensions-compose-jetbrains:$decomposeVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.foundation)
                implementation(compose.desktop.currentOs)
                val multiplatformSettingsVersion = "1.0.0"
                implementation("com.russhwolf:multiplatform-settings:$multiplatformSettingsVersion")
                implementation("com.russhwolf:multiplatform-settings-coroutines:$multiplatformSettingsVersion")
//                implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
//                implementation("com.arkivanov.decompose:extensions-compose-jetbrains:$decomposeVersion")
            }
        }
    }
}

//multiplatformResources {
//    multiplatformResourcesPackage = "me.frauenfelderflorian.tournamentscompose.common"
//}

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
    room {
        schemaDirectory("$projectDir/schemas/")
    }
}
