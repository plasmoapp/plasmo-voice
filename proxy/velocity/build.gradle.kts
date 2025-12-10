import su.plo.voice.extension.javaVersion
import su.plo.voice.extension.slibPlatform

plugins {
    id("su.plo.voice.shadow")
    id("su.plo.voice.maven-publish")
    id("kotlin-kapt")
}

group = "$group.proxy"

base.archivesName.set("${rootProject.name}-Velocity")
javaVersion = 11

dependencies {
    compileOnly(libs.velocity)
    kapt(libs.velocity)
    compileOnly(libs.versions.bstats.map { "org.bstats:bstats-velocity:$it" })

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
    shadow(libs.versions.bstats.map { "org.bstats:bstats-velocity:$it" })

    slibPlatform(
        "velocity",
        libs.versions.slib.get(),
        implementation = ::compileOnly,
        shadow = ::shadow
    )
}
