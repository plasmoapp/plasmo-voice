import su.plo.voice.util.copyJarToRootProject

plugins {
    id("su.plo.voice.relocate")
    id("su.plo.voice.relocate-guice")
}

group = "$group.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.papi)
    compileOnly(libs.supervanish)

    compileOnly("su.plo.ustats:paper:${libs.versions.ustats.get()}")
    compileOnly("su.plo.slib:spigot:${libs.versions.slib.get()}")

    compileOnly(project(":server:common"))

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:server"),
        project(":api:server-common"),
        project(":server:common"),
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
    shadow("su.plo.ustats:paper:${libs.versions.ustats.get()}")
    shadow("su.plo.slib:spigot:${libs.versions.slib.get()}") {
        isTransitive = false
    }
}

tasks {
    processResources {
        filesMatching(mutableListOf("plugin.yml", "paper-plugin.yml")) {
            expand(
                mutableMapOf(
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Paper")
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

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
