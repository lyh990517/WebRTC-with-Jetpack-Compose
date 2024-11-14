plugins {
    id("webrtc.android.feature")
}

android {
    namespace = "com.example.main"
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(projects.feature.config)
    implementation(projects.feature.connect)
}