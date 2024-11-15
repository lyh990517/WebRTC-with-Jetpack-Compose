plugins {
    id("webrtc.android.library")
    id("webrtc.android.hilt")
    id("webrtc.firebase")
}

android {
    namespace = "com.example.domain"
}


dependencies {
    api(projects.common.util)
    api(libs.google.webrtc)
}