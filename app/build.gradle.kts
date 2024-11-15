import com.example.build_logic.setNamespace

plugins {
    id("app")
}

android {
    setNamespace("app")
}

dependencies {
    implementation(projects.feature.main)

    runtimeOnly(projects.core.webrtc.client)
    runtimeOnly(projects.core.webrtc.signaling)
    runtimeOnly(projects.core.webrtc.controller)
}
