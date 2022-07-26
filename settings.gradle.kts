pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "PlasmoVoice"

// API
include("api:common")
