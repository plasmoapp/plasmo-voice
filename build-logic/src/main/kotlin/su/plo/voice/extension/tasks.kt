package su.plo.voice.extension

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.expand
import org.gradle.language.jvm.tasks.ProcessResources

var Project.javaVersion: Int
    get() = extensions.getByType<JavaPluginExtension>(JavaPluginExtension::class.java).toolchain.languageVersion.get().asInt()
    set(value) {
        extensions.getByType<JavaPluginExtension>(JavaPluginExtension::class.java).toolchain.languageVersion.set(JavaLanguageVersion.of(value))
    }

fun ProcessResources.expandMatching(
    match: List<String>,
    vararg properties: Pair<String, Any>,
) {
    doFirst {
        filesMatching(match) {
            expand(*properties)
        }
    }
}
