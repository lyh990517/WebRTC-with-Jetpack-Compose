plugins {
    id("webrtc.android.library")
    id("webrtc.android.hilt")
}

android {
    namespace = "com.example.data"
}


dependencies {
    implementation(project(":domain"))

    implementation("org.webrtc:google-webrtc:1.0.30039@aar")
}