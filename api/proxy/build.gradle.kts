val mavenGroup: String by rootProject
group = "$mavenGroup.api"

val javadocProjects = listOf(
    project(":api:proxy"),
    project(":api:common"),
    project(":api:server-common"),
    project(":protocol")
)

configurations.shadow {
    isTransitive = false
}

dependencies {
    implementation(shadow(project(":api:common"))!!)
    implementation(shadow(project(":api:server-common"))!!)
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
