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
    implementation(projects.feature.call)

    implementation(libs.google.webrtc)
}