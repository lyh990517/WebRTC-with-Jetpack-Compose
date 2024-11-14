import com.example.build_logic.setNamespace

plugins {
    id("core.base")
}

android {
    setNamespace("com.example.util")
}

dependencies {
    implementation(libs.google.webrtc)
}
