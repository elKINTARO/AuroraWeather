@file:Suppress("DEPRECATION")

plugins {
    id("com.android.application")

}

android {
    namespace = "com.example.auroraweather"
    compileSdkVersion(35)
    defaultConfig {
        applicationId = "com.example.auroraweather"
        minSdkVersion(26)
        targetSdkVersion(34)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(libs.appcompat.v161)
    implementation(libs.constraintlayout.v214)
    implementation(libs.material.v1110)
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Lottie для анімацій
    implementation(libs.lottie)

    // Volley для мережевих запитів
    implementation(libs.volley)

    // Google Play Services Location
    implementation (libs.play.services.location)

    // Тестування
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)
}