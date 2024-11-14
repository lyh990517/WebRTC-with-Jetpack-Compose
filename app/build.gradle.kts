plugins {
    id("app")
    id("firebase")
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
}

dependencies {
    implementation(projects.feature.main)

    runtimeOnly(projects.core.firestore.impl)
    runtimeOnly(projects.core.webrtc.impl)
}
