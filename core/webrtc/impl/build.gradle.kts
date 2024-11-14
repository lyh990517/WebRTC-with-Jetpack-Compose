import com.example.build_logic.setNamespace

plugins {
    id("library")
    id("firebase")
}

android {
    setNamespace("com.example.webrtc.impl")
}

dependencies {
    implementation(projects.core.webrtc.api)
    implementation(projects.core.firestore.api)

    implementation(libs.google.webrtc)
}