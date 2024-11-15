plugins {
    id("webrtc.android.library")
    id("webrtc.android.compose")
}

android {
    namespace = "com.example.designsystem"
}

dependencies {
    api(libs.google.webrtc)
}