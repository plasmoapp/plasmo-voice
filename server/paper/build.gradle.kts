import su.plo.voice.extension.expandMatching
import su.plo.voice.extension.javaVersion
import su.plo.voice.extension.slibPlatform

plugins {
    id("su.plo.voice.shadow")
    id("su.plo.voice.maven-publish")
}

group = "$group.server"

base.archivesName.set("${rootProject.name}-Paper")
javaVersion = 17

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.papi)
    compileOnly(libs.supervanish)

    compileOnly("org.bstats:bstats-bukkit:${libs.versions.bstats.get()}")

    api(project(":server:common"))

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:server"),
        project(":api:server-proxy-common"),
        project(":server:common"),
        project(":server-proxy-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it)
    }

    // shadow external deps
    shadow("org.bstats:bstats-bukkit:${libs.versions.bstats.get()}")

    slibPlatform(
        "spigot",
        libs.versions.slib.get(),
        implementation = ::compileOnly,
        shadow = ::shadow
    )
}

tasks {
    processResources {
        expandMatching(
            listOf("plugin.yml", "paper-plugin.yml"),
            "version" to version
        )
    }
}
