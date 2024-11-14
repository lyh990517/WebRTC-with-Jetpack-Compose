package com.example.build_logic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureFirebase() {
    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            add("implementation", platform(libs.findLibrary("firebase.bom").get()))
            add("implementation", libs.findLibrary("firebase.analytics.ktx").get())
            add("implementation", libs.findLibrary("firebase.firestore.ktx").get())
        }
    }
}