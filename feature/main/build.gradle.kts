plugins {
    id("webrtc.android.feature")
}

android {
    namespace = "com.example.main"
}


dependencies {
    implementation(projects.feature.config)
    implementation(projects.feature.connect)
    api(libs.androidx.activity.compose)
}