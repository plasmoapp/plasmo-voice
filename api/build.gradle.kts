subprojects {
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
