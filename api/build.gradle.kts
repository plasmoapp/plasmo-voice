@file:OptIn(ExperimentalAbiValidation::class)

import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

subprojects {
    group = "$group.api"

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "su.plo.voice.maven-publish")

    kotlin {
        abiValidation()
    }

    configure<DokkaExtension> {
        modulePath.set(project.name)
        dokkaSourceSets.configureEach {
            externalDocumentationLinks {
                register("slib") {
                    url.set(URI("https://slib.plasmoverse.com/"))
                    packageListUrl.set(URI("https://slib.plasmoverse.com/package-list"))
                }
            }
        }
    }

    dependencies {
        api("com.google.guava:guava") { version { prefer(rootProject.libs.versions.guava.get()) } }
        api("com.google.code.gson:gson") { version { prefer(rootProject.libs.versions.gson.get()) } }
        api(rootProject.libs.config)
        api(project(":protocol"))
    }

    tasks.named("check") {
        dependsOn(tasks.named("checkLegacyAbi"))
    }
}

dependencies {
    subprojects.forEach { dokka(it) }
}

tasks.jar {
    enabled = false
}
