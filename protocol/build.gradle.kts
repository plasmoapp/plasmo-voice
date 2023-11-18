plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("su.plo.slib:api-common:${libs.versions.slib.get()}")
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
