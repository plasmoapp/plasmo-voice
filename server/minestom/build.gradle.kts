import su.plo.voice.extension.slibPlatform
import su.plo.voice.util.copyJarToRootProject

plugins {
    id("su.plo.voice.relocate")
    id("su.plo.voice.maven-publish")
}

group = "$group.server"

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.minestom.extension)

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
        shadow(it) { isTransitive = false }
    }

    // shadow external deps
    shadow(kotlin("stdlib-jdk8"))
    shadow(libs.kotlinx.coroutines)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.json)
    shadow(libs.guava)
    shadow(libs.netty)

    shadow(libs.opus.jni)
    shadow(libs.opus.concentus)
    shadow(libs.config)
    shadow(libs.crowdin) { isTransitive = false }

    slibPlatform(
        "minestom",
        libs.versions.slib.get(),
        implementation = ::compileOnly,
        shadow = ::shadow
    )
}

tasks {
    processResources {
        filesMatching(mutableListOf("extension.json")) {
            expand(
                    mutableMapOf(
                            "version" to version
                    )
            )
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Minestom")
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
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
