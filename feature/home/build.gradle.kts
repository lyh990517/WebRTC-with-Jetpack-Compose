import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("firebase")
}

android {
    setNamespace("com.example.home")
}

dependencies {
    api(project(":domain"))
    implementation(project(":data"))
}
