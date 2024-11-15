import com.example.build_logic.setNamespace

plugins {
    id("feature")
    id("activity")
}

android {
    setNamespace("feature.main")
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.call)
}
