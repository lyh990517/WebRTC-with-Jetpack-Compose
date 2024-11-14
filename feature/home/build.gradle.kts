import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("firebase")
}

android {
    setNamespace("com.example.home")
}

dependencies {
    implementation(projects.core.firestore.api)
    implementation(projects.core.webrtc.api)
}
