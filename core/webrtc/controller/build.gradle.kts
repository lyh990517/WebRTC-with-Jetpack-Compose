import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("core.webrtc.controller")
}

dependencies {
    implementation(projects.core.webrtc.common)

    implementation(libs.google.webrtc)
}
