import org.gradle.kotlin.dsl.support.listFilesOrdered

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()

        maven("https://jitpack.io/")
        maven("https://maven.fabricmc.net")

        maven("https://maven.architectury.dev/")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://repo.plasmoverse.com/snapshots")
    }

    plugins {
        val egtVersion = "0.8.5-SNAPSHOT"
        id("gg.essential.defaults") version egtVersion
        id("gg.essential.multi-version.root") version egtVersion
    }
}

plugins {
    id("su.plo.voice.client-projects")
}

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

// Server-Proxy Common (Module for common code between server and proxy implementations)
include("server-proxy-common")

// Server
file("server").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach { file ->
    include("server:${file.name}")

    file.listFilesOrdered { it.isDirectory && it.name == "api" }
        .forEach { _ -> include("server:${file.name}:api") }
}

// Proxy
file("proxy").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("proxy:${it.name}")
}
