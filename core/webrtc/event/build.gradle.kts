import com.example.build_logic.setNamespace

plugins {
    id("core.base")
}

android {
    setNamespace("core.webrtc.event")
}

dependencies {
    implementation(libs.google.webrtc)
}