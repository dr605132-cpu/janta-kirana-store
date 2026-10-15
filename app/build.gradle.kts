plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jantakirana.store"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jantakirana.store"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Barcode scanner
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
}
