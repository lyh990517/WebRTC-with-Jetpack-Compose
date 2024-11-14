import com.example.build_logic.setNamespace

plugins {
    id("core.base")
}

android {
    setNamespace("com.example.util")
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.google.webrtc)
}
