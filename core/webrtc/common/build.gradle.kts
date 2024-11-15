import com.example.build_logic.setNamespace

plugins {
    id("core.base")
}

android {
    setNamespace("core.webrtc.common")
}

dependencies {
    implementation(libs.google.webrtc)
}