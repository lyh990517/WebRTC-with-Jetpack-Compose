import com.example.build_logic.setNamespace

plugins {
    id("app")
    id("firebase")
}

android {
    setNamespace("app")
}

dependencies {
    implementation(projects.feature.main)

    runtimeOnly(projects.webrtcSdk)
}
