import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("firebase")
}

android {
    setNamespace("core.webrtc.firestore")
}

dependencies {
    implementation(projects.core.webrtc.event)

    implementation(libs.google.webrtc)
}
