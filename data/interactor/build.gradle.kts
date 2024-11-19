plugins {
    id("webrtc.android.library")
    id("webrtc.android.hilt")
    id("webrtc.firebase")
}

android {
    namespace = "com.example.interactor"
}

dependencies {
    api(libs.google.webrtc)
    implementation(projects.domain)
}