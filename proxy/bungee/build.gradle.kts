import su.plo.voice.extension.expandMatching
import su.plo.voice.extension.javaVersion
import su.plo.voice.extension.slibPlatform

plugins {
    id("su.plo.voice.shadow")
    id("su.plo.voice.maven-publish")
}

group = "$group.proxy"

base.archivesName.set("${rootProject.name}-BungeeCord")
javaVersion = 8

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly("org.bstats:bstats-bungeecord:${libs.versions.bstats.get()}")

    api(project(":proxy:common"))
    compileOnly(libs.netty)

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:proxy"),
        project(":api:server-proxy-common"),
        project(":proxy:common"),
        project(":server-proxy-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it)
    }

    // shadow external deps
    shadow("org.bstats:bstats-bungeecord:${libs.versions.bstats.get()}")

    slibPlatform(
        "bungee",
        libs.versions.slib.get(),
        implementation = ::compileOnly,
        shadow = ::shadow
    )
}

tasks {
    processResources {
        expandMatching(
            listOf("bungee.yml"),
            "version" to version,
        )
    }
}
