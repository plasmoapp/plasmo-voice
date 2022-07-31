import org.gradle.kotlin.dsl.support.listFilesOrdered

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
file("api").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("api:${it.name}")
}

// Common
include("common")

// Client
include("client:common")
// Versions
file("client/versions").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("client:versions:${it.name}")
    include("client:versions:${it.name}:common")
    include("client:versions:${it.name}:fabric")
}

// Server
file("server").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("server:${it.name}")
}
