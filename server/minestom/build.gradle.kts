import su.plo.voice.extension.javaVersion
import su.plo.voice.extension.slibPlatform

plugins {
    id("su.plo.voice.maven-publish")
}

group = "$group.server"

javaVersion = 21

dependencies {
    compileOnly(libs.minestom)

    api(project(":server:common"))

    api(libs.opus.jni)
    api(libs.opus.concentus)
    api(libs.config)
    api(libs.crowdin) { isTransitive = false }

    runtimeOnly(libs.guava)
    runtimeOnly(libs.netty)

    slibPlatform(
        "minestom",
        libs.versions.slib.get(),
        implementation = ::api
    )
}
