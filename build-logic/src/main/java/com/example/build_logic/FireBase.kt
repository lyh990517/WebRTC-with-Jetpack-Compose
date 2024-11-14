package com.example.build_logic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureFirebase() {
    val libs = extensions.libs

    with(plugins){
        apply("com.google.gms.google-services")
    }

    androidExtension.apply {
        dependencies {
            add("implementation", platform(libs.findLibrary("firebase.bom").get()))
            add("implementation", libs.findLibrary("firebase.analytics.ktx").get())
            add("implementation", libs.findLibrary("firebase.firestore.ktx").get())
        }
    }
}