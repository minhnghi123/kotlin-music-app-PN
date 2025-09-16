plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.musicapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.musicapp"
        minSdk = 21
        targetSdk = 36
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
}

dependencies {
// Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OkHttp logging (xem log request/response cho dễ debug)
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
// Glide (load ảnh coverImage)
    implementation("com.github.bumptech.glide:glide:4.16.0")
//EXOPLAYER
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
// ExoPlayer (Media3)
//    others
    // Room components
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}