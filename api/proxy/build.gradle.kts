val mavenGroup: String by rootProject
group = "$mavenGroup.api"

plugins {
    id("su.plo.voice.maven-publish")
}

dependencies {
    shadow(implementation(project(":api:common"))!!)
    shadow(implementation(project(":api:server-common"))!!)
    shadow(implementation(project(":protocol"))!!)
}

tasks {
    val javadocProjects = listOf(
        project(":api:proxy"),
        project(":api:common"),
        project(":api:server-common"),
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
