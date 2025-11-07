import java.util.Properties

val localProperties = rootProject.file("local.properties")
val properties = Properties().apply {
    if (localProperties.exists()) {
        localProperties.inputStream().use { load(it) }
    }
}
val mapsApiKey = properties.getProperty("MAPS_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}


android {
    namespace = "com.example.mytravelcompanion"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mytravelcompanion"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        resValue("string", "google_maps_key", mapsApiKey)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix= ".debug"
            isDebuggable = true
        }
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
}

dependencies {

    //immagini
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.google.code.gson:gson:2.10.1")
    //map compose
    implementation("com.google.maps.android:maps-compose:2.14.0")
    implementation("com.google.maps.android:maps-utils-ktx:5.1.1")

    //maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui.graphics)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.9.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}