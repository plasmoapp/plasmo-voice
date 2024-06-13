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
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://repo.plo.su")
        maven("https://repo.plasmoverse.com/snapshots")
    }

    plugins {
        val egtVersion = "0.6.0-SNAPSHOT"
        id("gg.essential.defaults") version egtVersion
        id("gg.essential.multi-version.root") version egtVersion
    }
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

// Server Common (Module for common code between server and proxy implementations)
include("server-common")

// Server
file("server").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("server:${it.name}")
}

// Proxy
file("proxy").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("proxy:${it.name}")
}
