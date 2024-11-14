import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("activity")
    id("firebase")
}

android {
    setNamespace("com.example.main")
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.core.webrtc.api)
    implementation(projects.core.firestore.api)

    implementation(libs.google.webrtc)
}