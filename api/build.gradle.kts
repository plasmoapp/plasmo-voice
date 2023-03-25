subprojects {
    dependencies {
        api(rootProject.libs.guice)
        api(rootProject.libs.guava)
        api(rootProject.libs.gson)
        api(rootProject.libs.config)
        api(project(":protocol"))
    }

    if (this.name == "common") return@subprojects

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
//            shadow.component(this)

//            from(components["java"])

            artifact(tasks.getByName("shadowJar")) {
                artifactId = project.name
                classifier = ""
            }

            pom.withXml {
                val dependenciesNode = this.asNode().appendNode("dependencies")

                configurations.api.get().allDependencies.forEach {
                    if (it is ProjectDependency) return@forEach

                    println(it.name)

                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.name)
                    dependencyNode.appendNode("version", it.version)
                }

            }

            artifact(tasks.getByName("sourcesJar")) {
                artifactId = project.name
                classifier = "sources"
            }
//
//            artifact(tasks.javadocJar) {
//                artifactId = project.name
//                classifier = "javadoc"
//            }
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
}
