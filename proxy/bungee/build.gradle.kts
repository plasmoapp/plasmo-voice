import su.plo.voice.util.copyJarToRootProject

plugins {
    id("su.plo.voice.relocate")
    id("su.plo.voice.relocate-guice")
}

group = "$group.bungee"

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly("org.bstats:bstats-bungeecord:${libs.versions.bstats.get()}")
    compileOnly("su.plo.slib:bungee:${libs.versions.slib.get()}")

    compileOnly(project(":proxy:common"))
    compileOnly(libs.netty)

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:proxy"),
        project(":api:server-common"),
        project(":proxy:common"),
        project(":server-common"),
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

    shadow(libs.guice) {
        exclude("com.google.guava")
    }

    shadow(libs.opus.jni)
    shadow(libs.opus.concentus)
    shadow(libs.config)
    shadow(libs.crowdin) {
        isTransitive = false
    }
    shadow("org.bstats:bstats-bungeecord:${libs.versions.bstats.get()}")
    shadow("su.plo.slib:bungee:${libs.versions.slib.get()}") {
        isTransitive = false
    }
}

tasks {
    processResources {
        filesMatching("bungee.yml") {
            expand(
                mutableMapOf(
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-BungeeCord")
        archiveAppendix.set("")
        archiveClassifier.set("")

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

    jar {
        enabled = false
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
}
