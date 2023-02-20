package su.plo.voice

import org.gradle.kotlin.dsl.invoke

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow")
}

java.withJavadocJar()
java.withSourcesJar()

tasks {
    shadowJar {
        configurations = listOf(project.configurations.getByName("shadow"))
        archiveBaseName.set(project.name)
        archiveAppendix.set("")
        archiveClassifier.set("")
    }

    build {
        dependsOn.add(shadowJar)
    }

    jar {
        dependsOn.add(shadowJar)
    }
}

configure<PublishingExtension> {
    publications.create<MavenPublication>(project.name) {
        artifact(tasks.getByName("shadowJar")) {
            artifactId = project.name
            classifier = ""
        }

        artifact(tasks.getByName("sourcesJar")) {
            artifactId = project.name
            classifier = "sources"
        }

        artifact(tasks.getByName("javadocJar")) {
            artifactId = project.name
            classifier = "javadoc"
        }
    }

    repositories {
        val mavenUser = project.findProperty("maven_user")
        val mavenPassword = project.findProperty("maven_password")

        if (mavenUser != null && mavenPassword != null) {
            maven("https://repo.plo.su/public/") {
                credentials {
                    username = mavenUser.toString()
                    password = mavenPassword.toString()
                }
            }
        }
    }
}
