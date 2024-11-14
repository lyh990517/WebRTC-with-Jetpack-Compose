import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("firebase")
}

android {
    setNamespace("com.example.home")
}

dependencies {
    implementation(project(":core:firestore:api"))
    implementation(project(":core:webrtc:api"))
}
