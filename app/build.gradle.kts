plugins {
    id("app")
}

android {
    namespace = "com.example.webrtc"
}

dependencies {
    implementation(projects.feature.main)

    runtimeOnly(projects.core.webrtc.impl)
}
