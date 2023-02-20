val mavenGroup: String by rootProject
group = "$mavenGroup.api"

plugins {
    id("su.plo.voice.maven-publish")
}

dependencies {
    shadow(implementation(project(":api:common"))!!)
    shadow(implementation(project(":protocol"))!!)
    implementation(libs.config)
    shadow(libs.config)
}

tasks {
    val javadocProjects = listOf(
        project(":api:client"),
        project(":api:common"),
        project(":protocol")
    )

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
