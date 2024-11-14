plugins {
    id("webrtc.android.library")
}

android {
    namespace = "com.example.screen"
}

dependencies {
    api(libs.circuit.runtime)
}