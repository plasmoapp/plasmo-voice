val mavenGroup: String by rootProject
group = "$mavenGroup"

dependencies {
    implementation("su.plo.slib:api-common:${rootProject.libs.versions.crosslib.get()}")
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
