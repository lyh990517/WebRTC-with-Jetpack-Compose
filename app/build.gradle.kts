plugins {
    id("app")
    id("firebase")
}

android {
    namespace = "com.example.webrtc"
}

dependencies {
    implementation(projects.feature.main)

    runtimeOnly(projects.core.webrtc.impl)
}
