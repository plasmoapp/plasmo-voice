val mavenGroup: String by rootProject

val paperVersion: String by project

group = "$mavenGroup.paper"

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:${paperVersion}")

    implementation(project(":server:common"))

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:server"),
        project(":api:server-common"),
        project(":server:common"),
        project(":server-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it) {
            isTransitive = false
        }
    }
    // shadow external deps
    shadow(rootProject.libs.guice)
    shadow(rootProject.libs.opus)
    shadow(rootProject.libs.config)
    shadow(kotlin("stdlib-jdk8"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                mutableMapOf(
                    "version" to version
                )
            )
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Paper")
        archiveAppendix.set("")
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))
            exclude(dependency("com.google.guava:guava"))

            exclude("su/plo/opus/*")
            exclude("natives/opus/**/*")
            exclude("META-INF/**")
        }
    }

    build {
        dependsOn.add(shadowJar)

        doLast {
            shadowJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${shadowJar.get().archiveFile.get().asFile.name}"), true)
        }
    }

    jar {
        archiveClassifier.set("dev")
        dependsOn.add(shadowJar)
    }
}
