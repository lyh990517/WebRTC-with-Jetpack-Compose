import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("core.webrtc.client")
}

dependencies {
    api(projects.core.webrtc.common)

    implementation(projects.core.webrtc.api)

    implementation(libs.google.webrtc)
}
