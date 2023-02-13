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

    implementation(project(":api:common"))
    implementation(project(":api:server-common"))
    implementation(project(":api:proxy"))
    
    implementation(project(":proxy:common"))
    implementation(project(":server-common"))
    implementation(project(":common"))

    implementation(project(":protocol"))

    compileOnly(rootProject.libs.netty)
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
        archiveBaseName.set("PlasmoVoice-Velocity")
        archiveAppendix.set("")
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))

            exclude("su/plo/opus/*")
            exclude("natives/opus/**/*")
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
