val mavenGroup: String by rootProject

val paperVersion: String by project
val placeholderApiVersion: String by project

group = "$mavenGroup.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${paperVersion}")
    compileOnly("me.clip:placeholderapi:${placeholderApiVersion}")
    compileOnly(rootProject.libs.versions.ustats.map { "su.plo.ustats:paper:$it" })

    compileOnly(project(":server:common"))

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
    shadow(rootProject.libs.crowdin.lib)
    shadow(kotlin("stdlib-jdk8"))
    shadow(rootProject.libs.kotlinx.coroutines)
    shadow(rootProject.libs.kotlinx.json)
    shadow(rootProject.libs.versions.ustats.map { "su.plo.ustats:paper:$it" })
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

        relocate("su.plo.crowdin", "su.plo.voice.crowdin")
        relocate("su.plo.ustats", "su.plo.voice.ustats")

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

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}
