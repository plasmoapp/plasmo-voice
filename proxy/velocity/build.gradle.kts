import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.*

val mavenGroup: String by rootProject

val velocityVersion: String by project
val buildVersion: String by rootProject

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

group = "$mavenGroup.velocity"

dependencies {
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityVersion")

    api(project(":proxy:common"))
    compileOnly(rootProject.libs.netty)

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:proxy"),
        project(":api:server-common"),
        project(":proxy:common"),
        project(":server-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it) {
            isTransitive = false
        }
    }
    // shadow external deps
    shadow(rootProject.libs.opus)
    shadow(rootProject.libs.config)
    shadow(kotlin("stdlib-jdk8"))
    shadow(rootProject.libs.kotlinx.coroutines)
    shadow(rootProject.libs.kotlinx.json)
}

val templateSource = file("src/main/templates")
val templateDestination = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mutableMapOf(
        "version" to buildVersion
    )

    inputs.properties(props)

    from(templateSource)
    into(templateDestination)
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { it.outputs })

fun Project.idea(block: IdeaModel.() -> Unit) =
    (this as ExtensionAware).extensions.configure("idea", block)

fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
    (this@settings as ExtensionAware).extensions.configure(block)

@Suppress("UNCHECKED_CAST")
val ProjectSettings.taskTriggers: TaskTriggersConfig
    get() = (this as ExtensionAware).extensions.getByName("taskTriggers") as TaskTriggersConfig

rootProject.idea {
    project {
        settings {
            taskTriggers {
                afterSync(generateTemplates)
                beforeBuild(generateTemplates)
            }
        }
    }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Velocity")
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

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
