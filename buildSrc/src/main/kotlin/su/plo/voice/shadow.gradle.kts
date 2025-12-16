package su.plo.voice

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import su.plo.voice.util.DeduplicatingLicenseTransformer

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
//    "org.slf4j:slf4j-api",
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

            configurations.shadow
                .get()
                .resolvedConfiguration
                .resolvedArtifacts
                .map { it.moduleVersion.id.toString() }
                .filter { !isExcluded(it) }
                .sorted()
                .forEach { println(it) }
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

        reloc("su.plo.crowdin", "crowdin")
        reloc("org.bstats", "bstats")

        reloc("org.concentus", "concentus")

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

    val copyTask = register<Copy>("copyJarToRootProject") {
        from(finalJar)
        into(rootProject.layout.buildDirectory.dir("libs"))
    }

    build {
        dependsOn(copyTask)
    }
}
