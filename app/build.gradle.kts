plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kapt) // For Hilt and Room annotation processing
    alias(libs.plugins.hilt) // Hilt plugin
}

android {
    namespace = "com.codewithpk.palmpay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codewithpk.palmpay"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // *** CRITICAL FIX: Update this to be compatible with Kotlin 2.0.21 ***
        // For Kotlin 2.0.x, you generally need Compose Compiler 1.6.10 or newer.
        // As of now, 1.6.10 is the stable version for Kotlin 2.0.x.
        kotlinCompilerExtensionVersion = "1.6.10"
    }

}

dependencies {
// --- Core AndroidX Libraries ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)


    // --- Navigation Compose ---
    implementation(libs.navigation.compose) 

    // --- Hilt (Dependency Injection) ---
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler) 
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)


    // --- Room (Local DB) ---
    implementation(libs.room.runtime)
    kapt(libs.room.compiler) 
    implementation(libs.room.ktx)

    // --- CameraX (QR & PreviewView) ---
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // --- Biometric Auth ---
    implementation(libs.biometric)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}