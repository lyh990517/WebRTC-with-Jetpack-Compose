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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":feature")
include(":core")
include(":feature:main")
include(":feature:home")
include(":core:webrtc")
include(":core:webrtc:api")
include(":core:webrtc:impl")
include(":feature:call")
include(":core:model")
include(":core:util")
include(":core:designsystem")
