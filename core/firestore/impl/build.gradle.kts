import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("com.example.impl")
}

dependencies {
    implementation(projects.core.firestore.api)

    implementation(libs.google.webrtc)
}