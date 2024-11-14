import com.example.build_logic.setNamespace

plugins {
    id("core")
}

android {
    setNamespace("com.example.webrtc.api")
}

dependencies {
    implementation(libs.google.webrtc)
}
