import org.gradle.kotlin.dsl.support.listFilesOrdered

pluginManagement {
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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

include("client")
project(":client").apply {
    projectDir = file("client/")
    buildFileName = "root.gradle.kts"
}

file("client").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name.contains("-")
}.forEach {
    include("client:${it.name}")
    project(":client:${it.name}").apply {
        projectDir = file("client/${it.name}")
        buildFileName = "../build.gradle.kts"
    }
}

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

// macOS microphone helper
include("macos:protocol")
include("macos:helper")
include("macos:probe")
