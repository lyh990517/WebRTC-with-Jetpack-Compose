import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("core.webrtc.impl")
}

dependencies {
    implementation(projects.core.webrtc.api)
    implementation(projects.core.webrtc.manager)

    implementation(libs.google.webrtc)
}
