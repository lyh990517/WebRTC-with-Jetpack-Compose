import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("com.example.api")
}

dependencies {
    implementation(libs.google.webrtc)
}