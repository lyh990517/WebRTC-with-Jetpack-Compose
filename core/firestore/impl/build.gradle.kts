import com.example.build_logic.setNamespace

plugins {
    id("library")
    id("firebase")
}

android {
    setNamespace("com.example.impl")
}

dependencies {
    implementation(project(":core:firestore:api"))
    implementation(libs.google.webrtc)
}