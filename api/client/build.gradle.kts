val mavenGroup: String by rootProject
group = "$mavenGroup.api"

val javadocProjects = listOf(
    project(":api:client"),
    project(":api:common"),
    project(":protocol")
)

dependencies {
    implementation(project(":api:common"))
    shadow(project(":api:common"))

    implementation(project(":protocol"))
    shadow(project(":protocol"))

    implementation(libs.config)
    shadow(libs.config)
}

tasks {
    shadowJar {
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

    javadoc {
        source(javadocProjects.map {
            it.sourceSets.main.get().allJava
        })

        classpath = files(javadocProjects.map { it.sourceSets.main.get().compileClasspath })
        setDestinationDir(file("${buildDir}/docs/javadoc"))
    }

    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(javadocProjects.map {
            it.sourceSets.main.get().allSource
        })
    }
}

configure<PublishingExtension> {
    publications.create<MavenPublication>(project.name) {
        artifact(tasks.getByName("shadowJar")) {
            artifactId = project.name
            classifier = ""
        }

        artifact(tasks.sourcesJar) {
            artifactId = project.name
            classifier = "sources"
        }

        artifact(tasks.javadocJar) {
            artifactId = project.name
            classifier = "javadoc"
        }
    }
}
