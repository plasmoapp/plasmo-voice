package su.plo.voice.extension

import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency

typealias GradleModuleFunction = (module: String, action: Action<ExternalModuleDependency>) -> Unit

fun slibPlatform(
    platform: String,
    version: String,
    implementation: GradleModuleFunction,
    shadow: GradleModuleFunction? = null,
) {
    implementation("com.github.PadowYT2.mc-slib:$platform:$version") {}

    if (shadow == null) return

    shadow("com.github.PadowYT2.mc-slib:$platform:$version") { isTransitive = false }
}
