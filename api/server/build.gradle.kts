val mavenGroup: String by rootProject
group = "$mavenGroup.api"

val javadocProjects = listOf(
    project(":api:server"),
    project(":api:server-common"),
    project(":api:common"),
    project(":protocol")
)

dependencies {
    api(project(":api:common"))
    api(project(":api:server-common"))

    api("su.plo.slib:api-server:${rootProject.libs.versions.crosslib.get()}")
}

tasks {
//    javadoc {
//        source(javadocProjects.map {
//            it.sourceSets.main.get().allJava
//        })
//
//        classpath = files(javadocProjects.map { it.sourceSets.main.get().compileClasspath })
//        setDestinationDir(file("${buildDir}/docs/javadoc"))
//    }
//
    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(javadocProjects.map {
            it.sourceSets.main.get().allSource
        })
    }
}
