import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("core.webrtc.manager")
}

dependencies {
    implementation(libs.google.webrtc)
}
