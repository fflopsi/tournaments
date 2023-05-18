plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("dev.icerock.mobile.multiplatform-resources")
    id("kotlin-kapt")
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
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class) implementation(
                    compose.components.resources
                )
                implementation("dev.icerock.moko:resources:0.22.0")
                implementation("dev.icerock.moko:resources-compose:0.22.0")
                implementation("com.google.code.gson:gson:2.10.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.runtime:runtime-livedata")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
                implementation("androidx.navigation:navigation-compose:2.5.3")

                val roomVersion = "2.5.1"
                implementation("androidx.room:room-runtime:$roomVersion")
                implementation("androidx.room:room-ktx:$roomVersion")
                //annotationProcessor("androidx.room:room-compiler:$roomVersion")
                configurations["kapt"].dependencies.add(project.dependencies.create("androidx.room:room-compiler:$roomVersion"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.foundation)
                implementation(compose.desktop.currentOs)
                implementation("moe.tlaster:precompose:1.4.1")
            }
        }
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
