package com.example.webrtc

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies


internal fun Project.configureFirebase() {
    val libs = extensions.libs
    with(plugins) {
        apply("com.google.gms.google-services")
    }

    dependencies {
        val bom = libs.findLibrary("firebase-bom").get()
        add("implementation", platform(bom))
        add("implementation", libs.findLibrary("firebase.analytics.ktx").get())
        add("implementation", libs.findLibrary("firebase.firestore.ktx").get())
    }
}
