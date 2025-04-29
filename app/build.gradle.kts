plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.videoclub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.videoclub"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {

    implementation(libs.androidx.leanback)
    implementation(libs.glide)

    implementation(libs.androidx.leanback)
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Image loading
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // JSON parsing
    implementation("org.json:json:20230618")
}