val mavenGroup: String by rootProject

val paperVersion: String by project

group = "$mavenGroup.paper"

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:${paperVersion}")

    implementation(project(":api:common"))
    implementation(project(":api:server"))

    implementation(project(":server:common"))
    implementation(project(":common"))

    implementation(project(":protocol"))
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
        archiveBaseName.set("PlasmoVoice-Paper")
        archiveAppendix.set("")
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }
    }

    build {
        dependsOn.add(shadowJar)
    }

    jar {
        archiveClassifier.set("dev")
        dependsOn.add(shadowJar)
    }
}
