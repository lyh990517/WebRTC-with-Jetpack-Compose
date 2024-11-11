plugins {
    id("webrtc.android.library")
    id("webrtc.android.hilt")
}

android {
    namespace = "com.example.domain"
}


dependencies {
    implementation("org.webrtc:google-webrtc:1.0.30039@aar")
}