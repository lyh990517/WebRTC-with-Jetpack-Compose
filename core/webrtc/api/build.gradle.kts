import com.example.build_logic.setNamespace

plugins {
    id("library")
    id("firebase")
}

android {
    setNamespace("com.example.webrtc.api")
}

dependencies {
    implementation(libs.google.webrtc)
}