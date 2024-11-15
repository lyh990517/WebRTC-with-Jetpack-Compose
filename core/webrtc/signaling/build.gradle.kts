import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("core.webrtc.signaling")
}

dependencies {
    implementation(projects.core.webrtc.client)

    implementation(libs.google.webrtc)
}
