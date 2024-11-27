import com.example.build_logic.setNamespace

plugins {
    id("core")
    id("circuit")
}

android {
    setNamespace("circuit")
}

dependencies {
    implementation(libs.circuit.runtime)
    implementation(libs.circuit.foundation)
}