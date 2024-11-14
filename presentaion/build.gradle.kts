import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("firebase")
}

android {
    setNamespace("com.example.presentaion")
}

dependencies {
    api(project(":domain"))
    implementation(project(":data"))
}
