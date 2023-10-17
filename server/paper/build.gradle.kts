val mavenGroup: String by rootProject

val paperVersion: String by project
val placeholderApiVersion: String by project
val foliaVersion: String by project

plugins {
    id("su.plo.voice.relocate")
}

group = "$mavenGroup.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("dev.folia:folia-api:${foliaVersion}")
    compileOnly("io.papermc.paper:paper-api:${paperVersion}")
    compileOnly("me.clip:placeholderapi:${placeholderApiVersion}")

    compileOnly("su.plo.ustats:paper:${rootProject.libs.versions.ustats.get()}")
    compileOnly("su.plo.slib:spigot:${rootProject.libs.versions.crosslib.get()}")

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
    shadow(rootProject.libs.kotlinx.coroutines)
    shadow(rootProject.libs.kotlinx.coroutines.jdk8)
    shadow(rootProject.libs.kotlinx.json)

    shadow(rootProject.libs.guice) {
        exclude("com.google.guava")
    }

    shadow(rootProject.libs.opus)
    shadow(rootProject.libs.config)
    shadow(rootProject.libs.crowdin.lib) {
        isTransitive = false
    }
    shadow("su.plo.ustats:paper:${rootProject.libs.versions.ustats.get()}")
    shadow("su.plo.slib:spigot:${rootProject.libs.versions.crosslib.get()}") {
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

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("su.plo.ustats", "su.plo.voice.libs.ustats")

        relocate("com.google.inject", "su.plo.voice.libs.google.inject")
        relocate("org.aopalliance", "su.plo.voice.libs.aopalliance")
        relocate("javax.inject", "su.plo.voice.libs.javax.inject")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))

            exclude("su/plo/opus/*")
            exclude("natives/opus/**/*")

            exclude("DebugProbesKt.bin")
            exclude("META-INF/**")
        }
    }

    build {
        dependsOn.add(shadowJar)

        doLast {
            shadowJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${shadowJar.get().archiveFile.get().asFile.name}"), true)
        }
    }

    jar {
        archiveClassifier.set("dev")
        dependsOn.add(shadowJar)
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}
