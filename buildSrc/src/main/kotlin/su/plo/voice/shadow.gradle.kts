package su.plo.voice

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import su.plo.voice.util.DeduplicatingLicenseTransformer
import java.util.jar.Manifest
import java.util.zip.ZipFile

plugins {
    java
    id("com.gradleup.shadow")
}

val excludedDependencies = listOf(
    "net.java.dev.jna:jna",
    "org.jetbrains:annotations",
    "org.projectlombok:lombok",
    "com.google.guava:.*",
    "com.google.code.gson:gson",
    "com.google.code.findbugs:jsr305",
    "com.google.errorprone:error_prone_annotations",
    "com.google.j2objc:j2objc-annotations",
    "it.unimi.dsi:fastutil",
    "org.checkerframework:checker-qual",
    "com.mojang:brigadier",
//    "org.slf4j:slf4j-api",
)

// Class namespaces allowed in the final jar.
// Preventive step to avoid https://github.com/plasmoapp/plasmo-voice/issues/520 to happen again.
val allowedClassPrefixes = listOf(
    "su/plo/",          // own code + relocated libraries (su.plo.voice.libs.*)
    "com/plasmoverse/", // native JNI bindings
)

tasks {
    fun isExcluded(module: String): Boolean {
        return excludedDependencies.any { pattern ->
            val (groupPattern, namePattern) = pattern.split(":").take(2)
            val (group, name) = module.split(":").take(2)
            group.matches(groupPattern.toRegex()) && name.matches(namePattern.toRegex())
        }
    }

    register("printShadedDependencies") {
        doLast {
            println("=== Shaded dependencies ===")

            shadowJar.get().configurations.get()
                .forEach { configuration ->
                    configuration.resolvedConfiguration
                        .resolvedArtifacts
                        .map { it.moduleVersion.id.toString() }
                        .filter { !isExcluded(it) }
                        .sorted()
                        .forEach { println(it) }
                }
        }
    }

    shadowJar {
        dependsOn(jar)
        configurations = listOf(project.configurations.shadow.get())
        mergeServiceFiles()

        fun reloc(packageName: String, outputPackage: String = packageName) {
            relocate(packageName, "su.plo.voice.libs.$outputPackage")
        }

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("org.bstats", "su.plo.voice.libs.bstats")

        reloc("kotlin")
        reloc("kotlinx.coroutines")
        reloc("kotlinx.serialization")
        reloc("com.ensarsarajcic.kotlinx", "msgpack")

        reloc("su.plo.crowdin", "crowdin")
        reloc("org.bstats", "bstats")

        reloc("org.concentus", "concentus")

        reloc("at.favre.lib.hkdf", "hkdf")

        dependencies {
            excludedDependencies.forEach { exclude(dependency(it)) }
        }

        exclude("DebugProbesKt.bin")
        exclude("_COROUTINE/**")
        exclude("LICENSE*")
        exclude("README.md")
        exclude("**/*.kotlin_metadata")
        exclude("**/*.kotlin_module")
        exclude("**/*.kotlin_builtins")
    }

    val finalJar = register("finalJar", ShadowJar::class.java) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        if (tasks.any { it.name == "remapJar" }) {
            dependsOn("remapJar")
        }

        val archiveFile =
            (findByName("remapJar") as? AbstractArchiveTask)?.archiveFile
                ?: shadowJar.get().archiveFile

        from(zipTree(archiveFile)) {
            exclude("META-INF/**")
        }
        from(zipTree(archiveFile)) {
            include("META-INF/services/**")
            include("META-INF/jars/**")
            include("META-INF/MANIFEST.MF")
            include("META-INF/**.toml")
        }

        manifest {
            from({
                zipTree(archiveFile).first { it.name == "MANIFEST.MF" }
            }) {
                eachEntry {
                    if (key == "Multi-Release") {
                        exclude()
                    }
                }
            }
        }

        from(rootProject.file("LICENSE")) {
            into("META-INF/licenses")
        }

        from(
            project.configurations
                .shadow
                .get()
                .resolvedConfiguration
                .resolvedArtifacts
                .map { it.file }
                .map { if (it.isDirectory) it else zipTree(it) },
        ) {
            include("META-INF/LICENSE*")
            include("META-INF/NOTICE*")
            include("LICENSE*")

            eachFile {
                path = "META-INF/licenses/${path.substringAfterLast("/").removeSuffix(".txt")}"
            }

            includeEmptyDirs = false
        }

        transform(DeduplicatingLicenseTransformer::class.java)
    }

    val verifyShadedJar = register("verifyShadedJar") {
        dependsOn(finalJar)

        doLast {
            val jarFile = finalJar.get().archiveFile.get().asFile
            val offenders = ZipFile(jarFile).use { zip ->
                zip.entries().asSequence()
                    .map { it.name }
                    .filter { it.endsWith(".class") && !it.endsWith("module-info.class") }
                    .filter { name -> allowedClassPrefixes.none { name.startsWith(it) } }
                    .map { it.substringBeforeLast('/', "(default package)") }
                    .toSortedSet()
            }

            if (offenders.isNotEmpty()) {
                throw GradleException(
                    buildString {
                        appendLine("Unexpected packages shaded into ${jarFile.name}:")
                        offenders.forEach { appendLine("  $it") }
                        appendLine(
                            """
                                Relocate them under su.plo.voice.libs.* or exclude them in shadow.gradle.kts,
                                or add the package to allowedClassPrefixes if it is intentional.
                            """.trimIndent()
                        )
                    },
                )
            }
        }
    }

    // Verifies that every mixin config referenced in the loader metadata is actually bundled in the jar.
    // Preventive step to avoid the slib-forge.mixins.json crash from happening again.
    // (mixin was referenced in metadata, but the file was no longer shaded)
    val verifyMixinConfigs = register("verifyMixinConfigs") {
        dependsOn(finalJar)

        doLast {
            val jarFile = finalJar.get().archiveFile.get().asFile
            val missing = sortedSetOf<String>()

            ZipFile(jarFile).use { zip ->
                fun entryText(name: String): String? =
                    zip.getEntry(name)?.let { zip.getInputStream(it).bufferedReader().use { reader -> reader.readText() } }

                // source -> config
                val referenced = mutableListOf<Pair<String, String>>()

                // Fabric: "mixins": ["a.json", { "config": "b.json", "environment": "client" }]
                entryText("fabric.mod.json")?.let { json ->
                    Regex(""""mixins"\s*:\s*\[(.*?)]""", RegexOption.DOT_MATCHES_ALL)
                        .find(json)?.groupValues?.get(1)
                        ?.let { array ->
                            Regex(""""([^"]+\.json)"""").findAll(array).forEach {
                                referenced += "fabric.mod.json" to it.groupValues[1]
                            }
                        }
                }

                // NeoForge / Forge: [[mixins]] config="x.json"
                listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml").forEach { toml ->
                    entryText(toml)?.let { content ->
                        Regex("""config\s*=\s*"([^"]+)"""").findAll(content).forEach {
                            referenced += toml to it.groupValues[1]
                        }
                    }
                }

                // Forge: MixinConfigs manifest attribute (comma-separated)
                zip.getEntry("META-INF/MANIFEST.MF")?.let { entry ->
                    val manifest = zip.getInputStream(entry).use(::Manifest)
                    manifest.mainAttributes.getValue("MixinConfigs")
                        ?.split(",")
                        ?.map(String::trim)
                        ?.filter(String::isNotEmpty)
                        ?.forEach { referenced += "META-INF/MANIFEST.MF (MixinConfigs)" to it }
                }

                referenced
                    .filter { (_, config) -> zip.getEntry(config) == null }
                    .forEach { (source, config) -> missing += "$config (declared in $source)" }
            }

            if (missing.isNotEmpty()) {
                throw GradleException(
                    buildString {
                        appendLine("Mixin configs declared in metadata but missing from ${jarFile.name}:")
                        missing.forEach { appendLine("  $it") }
                        appendLine("  -> add the json to resources, stop excluding it, or drop the reference.")
                    },
                )
            }
        }
    }

    val copyTask = register<Copy>("copyJarToRootProject") {
        dependsOn(verifyShadedJar)
        dependsOn(verifyMixinConfigs)

        from(finalJar)
        into(rootProject.layout.buildDirectory.dir("libs"))
    }

    build {
        dependsOn(copyTask)
    }
}
