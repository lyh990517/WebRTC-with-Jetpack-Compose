import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("com.example.webrtc.impl")
}

dependencies {
    implementation(projects.core.webrtc.api)

    implementation(libs.google.webrtc)
}
