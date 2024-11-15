plugins {
    id("webrtc.android.library")
    id("webrtc.android.hilt")
    id("webrtc.firebase")
}

android {
    namespace = "com.example.interactor"
}

dependencies {
    api("org.webrtc:google-webrtc:1.0.30039@aar")
    implementation(projects.domain)
}