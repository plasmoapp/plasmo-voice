import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.*

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext")
}

dependencies {
    api(project(":api:common"))

    compileOnly(libs.netty)

    api(libs.config)
    api(libs.opus.jni)
    api(libs.opus.concentus)

    testImplementation(project(":api:common"))
    testImplementation(libs.guice)
}

val templateSource = file("src/main/java-templates")
val templateDestination: Provider<Directory> = layout.buildDirectory.dir("generated/sources/java-templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mutableMapOf(
        "version" to version
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
