import com.example.build_logic.setNamespace

plugins {
    id("feature")
}

android {
    setNamespace("com.example.call")
}

dependencies {
    implementation(projects.core.webrtc.api)

    implementation(libs.google.webrtc)
}
