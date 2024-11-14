plugins {
    id("app")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.webrtc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.webrtc"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":presentaion"))
    implementation(project(":feature:main"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.webrtc:google-webrtc:1.0.30039@aar")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.9.3")

    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.runtime:runtime:1.7.5")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
