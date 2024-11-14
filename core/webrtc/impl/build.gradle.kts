import com.example.build_logic.setNamespace

plugins {
    id("library")
    id("firebase")
}

android {
    setNamespace("com.example.webrtc.impl")
}

dependencies {
    implementation(project(":core:webrtc:api"))
    implementation(project(":core:firestore:api"))

    implementation(libs.google.webrtc)
}