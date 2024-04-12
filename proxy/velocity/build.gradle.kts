import su.plo.voice.extension.slibPlatform
import su.plo.voice.util.copyJarToRootProject

val velocityVersion: String by project

plugins {
    id("su.plo.voice.relocate")
    id("su.plo.voice.maven-publish")
    id("kotlin-kapt")
}

group = "$group.proxy"

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
        shadow(it) {
            isTransitive = false
        }
    }
    // shadow external deps
    shadow(kotlin("stdlib-jdk8"))
    shadow(libs.kotlinx.coroutines)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.json)

    shadow(libs.opus.jni)
    shadow(libs.opus.concentus)
    shadow(libs.config)
    shadow(libs.crowdin) {
        isTransitive = false
    }
    shadow(libs.versions.bstats.map { "org.bstats:bstats-velocity:$it" })

    slibPlatform(
        "velocity",
        "proxy",
        libs.versions.slib.get(),
        implementation = ::compileOnly,
        shadow = ::shadow
    )
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Velocity")
        archiveAppendix.set("")

        dependencies {
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude("META-INF/**")
        }
    }

    build {
        dependsOn.add(shadowJar)

        doLast {
            copyJarToRootProject(shadowJar.get())
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
