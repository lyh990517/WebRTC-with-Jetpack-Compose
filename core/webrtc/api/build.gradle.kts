import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("core.webrtc.api")
}

dependencies {
    implementation(libs.google.webrtc)
}
