package su.plo.voice

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

val platform: MavenPublishSettings = extensions.create("mavenPublish", MavenPublishSettings::class.java)

if (platform.skipShadow) {
    components.withType(AdhocComponentWithVariants::class.java).forEach { c ->
        c.withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) {
            skip()
        }
    }
}

tasks {
    java {
        withSourcesJar()
    }
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])

            if (rootProject.properties.containsKey("snapshot")) {
                version = "${rootProject.version}-SNAPSHOT" // removes a build version
            }

            artifactId = platform.artifactId ?: artifactId
        }
    }

    repositories {
        if (properties.containsKey("snapshot")) {
            maven("https://repo.plasmoverse.com/snapshots") {
                name = "PlasmoVerseSnapshots"

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        } else {
            maven("https://repo.plasmoverse.com/releases") {
                name = "PlasmoVerseReleases"

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}
