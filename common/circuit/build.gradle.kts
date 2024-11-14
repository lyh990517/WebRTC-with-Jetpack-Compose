plugins {
    id("webrtc.android.library")
    id("webrtc.android.compose")
}

android {
    namespace = "com.example.circuit"
}

dependencies {
    implementation(libs.circuit.foundation)
}