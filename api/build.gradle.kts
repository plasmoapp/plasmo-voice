subprojects {
    group = "$group.api"

    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        api(rootProject.libs.guava)
        api(rootProject.libs.gson)
        api(rootProject.libs.config)
        api(project(":protocol"))
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
}

tasks.jar {
    enabled = false
}
