pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://raw.githubusercontent.com/alexgreench/google-webrtc/master")
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://raw.githubusercontent.com/alexgreench/google-webrtc/master")
    }
}

rootProject.name = "WebRTC"
include(":app")
include(":presentaion")
include(":data")
include(":domain")