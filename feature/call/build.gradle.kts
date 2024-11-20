import com.example.build_logic.setNamespace

plugins {
    id("feature")
}

android {
    setNamespace("feature.call")
}

dependencies {
    implementation(projects.webrtcSdk)
}
