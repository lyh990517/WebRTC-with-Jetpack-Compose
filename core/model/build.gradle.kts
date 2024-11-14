import com.example.build_logic.setNamespace

plugins {
    id("core.model")
}

android {
    setNamespace("com.example.model")
}

dependencies {
    implementation(libs.google.webrtc)
}