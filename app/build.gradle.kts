plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.frostfitness.app"
    compileSdk = 36

    signingConfigs {
        create("frostRelease") {
            storeFile = file("frost-fitness.jks")
            storePassword = "frostfitness2026"
            keyAlias = "frost"
            keyPassword = "frostfitness2026"
        }
    }

    defaultConfig {
        applicationId = "com.frostfitness.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("frostRelease") }
        release { signingConfig = signingConfigs.getByName("frostRelease") }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
