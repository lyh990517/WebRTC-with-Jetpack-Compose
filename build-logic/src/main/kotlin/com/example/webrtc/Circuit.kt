package com.example.webrtc

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCircuit() {
    with(pluginManager) {
        apply("com.google.devtools.ksp")
    }
    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            add("implementation", libs.findBundle("circuit").get())
            add("api",libs.findLibrary("circuit.codegen.annotation").get())
            add("ksp", libs.findLibrary("circuit.codegen.ksp").get())
        }
    }
}