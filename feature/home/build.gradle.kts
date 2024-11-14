import com.example.build_logic.setNamespace

plugins {
    id("feature")
}

android {
    setNamespace("com.example.home")
}

dependencies {
    implementation(projects.core.webrtc.api)
}
