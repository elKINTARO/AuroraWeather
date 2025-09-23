@file:Suppress("DEPRECATION")

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.auroraweather"
    compileSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design компоненти (забезпечення повної сумісності)
    implementation("com.google.android.material:material:1.10.0")
    
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Lottie для анімацій
    implementation("com.airbnb.android:lottie:6.3.0")

    // Volley для мережевих запитів
    implementation("com.android.volley:volley:1.2.1")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Тестування
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}