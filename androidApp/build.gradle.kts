plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

kotlin {
    jvmToolchain((property("tournamentscompose.versions.java") as String).toInt())
}

android {
    namespace = "me.frauenfelderflorian.tournamentscompose.android"
    compileSdk = (property("tournamentscompose.android.compileTargetSdk") as String).toInt()

    defaultConfig {
        applicationId = "me.frauenfelderflorian.tournamentscompose"
        minSdk = (property("tournamentscompose.android.minSdk") as String).toInt()
        targetSdk = (property("tournamentscompose.android.compileTargetSdk") as String).toInt()
        versionCode = (property("tournamentscompose.android.versionCode") as String).toInt()
        versionName = property("tournamentscompose.version") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = property("tournamentscompose.versions.java") as String
    }
    kotlin {
        jvmToolchain((property("tournamentscompose.versions.java") as String).toInt())
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(project(":common"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")
}
