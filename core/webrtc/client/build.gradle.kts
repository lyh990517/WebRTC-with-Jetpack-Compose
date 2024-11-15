import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("core.webrtc.client")
}

dependencies {
    implementation(projects.core.webrtc.api)
    implementation(projects.core.webrtc.manager)
    implementation(projects.core.webrtc.event)
    implementation(projects.core.webrtc.signaling)

    implementation(libs.google.webrtc)
}
