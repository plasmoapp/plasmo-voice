subprojects {
    dependencies {
        api(rootProject.libs.guice)
        api(project(":protocol"))
    }

    if (this.name.contains("common")) return@subprojects

    dependencies {
        shadow(project(":protocol"))
    }

    tasks {
        java {
            withSourcesJar()
        }

        shadowJar {
            configurations = listOf(project.configurations.shadow.get())

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

//            artifact(tasks.sourcesJar) {
//                artifactId = project.name
//                classifier = "sources"
//            }
//
//            artifact(tasks.javadocJar) {
//                artifactId = project.name
//                classifier = "javadoc"
//            }
        }
    }
}
