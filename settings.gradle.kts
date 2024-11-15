pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://raw.githubusercontent.com/alexgreench/google-webrtc/master")
        }
        maven {
            // r8 maven
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://raw.githubusercontent.com/alexgreench/google-webrtc/master")
        }
    }
}
rootProject.name = "WebRTC"
include(":app")

//Data
include(
    ":data:interactor",
    ":data:firebase"
)

//Domain
include(
    ":domain"
)

//Feature
include(
    ":feature:main",
    ":feature:config",
    ":feature:connect"
)

//Core
include(
    ":core:designsystem"
)

//Common
include(
    ":common:circuit",
    ":common:screen",
    ":common:util",
)