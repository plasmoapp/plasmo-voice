package su.plo.voice.extension

import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.exclude

fun <T : ModuleDependency> T.excludeKotlin() {
    exclude(group = "org.jetbrains.kotlin")
}
