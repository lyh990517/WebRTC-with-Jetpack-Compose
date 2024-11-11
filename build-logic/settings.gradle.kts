
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    //toml 파일 추가
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}