pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()

        maven {
            name = "JitPack"
            url = uri("https://jitpack.io/")
        }

        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }

        maven {
            name = "Forge"
            url = uri("https://files.minecraftforge.net/maven/")
        }

        maven {
            name = "Architectury"
            url = uri("https://maven.architectury.dev/")
        }
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "PlasmoVoice"

// Protocol
include("protocol")

// API
include("api:common")
include("api:client")

include("common")

include("client:common")
include("client:versions:1_19")
include("client:versions:1_19:common")
include("client:versions:1_19:fabric")
