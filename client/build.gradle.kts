import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.RelocationTransform.Companion.registerRelocationAttribute
import gg.essential.gradle.util.noServerRunConfigs
import gg.essential.util.prebundleNow
import su.plo.config.toml.Toml

val mavenGroup: String by rootProject
val isMainProject = project.name == file("../mainProject").readText().trim()

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("su.plo.crowdin.plugin")
    id("su.plo.voice.relocate")
}

group = "$mavenGroup.client"
base.archivesName.set("plasmovoice-${platform.loaderStr}-${platform.mcVersionStr}")

loom.noServerRunConfigs()

if (platform.isForge) {
    loom.forge.apply {
        mixinConfig(
            "plasmovoice.mixins.json",
            "plasmovoice-forge.mixins.json"
        )
    }
}

loom.runs {
    getByName("client") {
        programArgs("--username", "GNOME__")
        property("plasmovoice.alpha.disableversioncheck", "true")
        property("universalcraft.shader.legacy.debug", "true")
    }
}

plasmoCrowdin {
    projectId = "plasmo-voice"
    sourceFileName = "client.json"
    resourceDir = "assets/plasmovoice/lang"
}

val shadowCommon by configurations.creating

val relocatedUC = registerRelocationAttribute("relocate-uc") {
    relocate("gg.essential.universal", "su.plo.voice.universal")
}
val universalCraft by configurations.creating {
    attributes { attribute(relocatedUC, true) }
}

fun uStatsVersion() = rootProject.libs.versions.ustats.map {
    val minecraftVersion = when (platform.mcVersion) {
        12001 -> "1.20"
        else -> platform.mcVersionStr
    }

    "${minecraftVersion}-${platform.loaderStr}:$it"
}.get()

fun universalCraftVersion() = rootProject.libs.versions.universalcraft.map {
    val minecraftVersion = when (platform.mcVersion) {
        11802 -> "1.18.1"
        else -> platform.mcVersionStr
    }

    "${minecraftVersion}-${platform.loaderStr}:$it"
}.get()

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
}

dependencies {
    compileOnly(rootProject.libs.netty)
    implementation(rootProject.libs.rnnoise)

    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11902 -> "0.73.2+1.19.2"
            11903 -> "0.73.2+1.19.3"
            11904 -> "0.76.0+1.19.4"
            12001 -> "0.84.0+1.20.1"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
        "include"(modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)
    }

    universalCraft("gg.essential:universalcraft-${universalCraftVersion()}") {
        isTransitive = false
    }
    modApi(prebundleNow(universalCraft))
    shadowCommon(prebundleNow(universalCraft))

    "su.plo.ustats:${uStatsVersion()}".also {
        modApi(it)
        shadowCommon(it) {
            isTransitive = false
        }
    }

    val includedProjects = listOf(
        ":api:common",
        ":api:client",
        ":api:server-common",
        ":api:server",
        ":server:common",
        ":server-common",
        ":common",
        ":protocol"
    )

    includedProjects.forEach {
        implementation(project(it))
        shadowCommon(project(it)) {
            isTransitive = false
        }
    }

    // kotlin
    shadowCommon(kotlin("stdlib-jdk8"))
    shadowCommon(rootProject.libs.kotlinx.coroutines)
    shadowCommon(rootProject.libs.kotlinx.coroutines.jdk8)
    shadowCommon(rootProject.libs.kotlinx.json)

    shadowCommon(rootProject.libs.opus)
    shadowCommon(rootProject.libs.config)
    shadowCommon(rootProject.libs.rnnoise)
    shadowCommon(rootProject.libs.crowdin.lib) {
        isTransitive = false
    }

    if (platform.isForge) {
        shadowCommon(rootProject.libs.guice) {
            exclude("com.google.guava")
        }
    } else {
        "include"(rootProject.libs.guice)
        "include"(rootProject.libs.aopalliance)
        "include"(rootProject.libs.javax.inject)
    }
}

tasks {
    processResources {
        val versionInfo = readVersionInfo()

        filesMatching(mutableListOf("META-INF/mods.toml")) {
            expand(
                mutableMapOf(
                    "version" to version,
                    "forgeVersion" to versionInfo.forgeVersion,
                    "mcVersions" to versionInfo.forgeMcVersions
                )
            )
        }

        filesMatching(mutableListOf("fabric.mod.json")) {
            expand(
                mutableMapOf(
                    "version" to version,
                    "mcVersions" to versionInfo.fabricMcVersions
                )
            )
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(plasmoCrowdinDownload)
    }

    jar {
        mergePlatformSpecifics()

        if (platform.mcVersion >= 11400) {
            excludeKotlinDefaultImpls()
        }
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")

        relocate("su.plo.ustats", "su.plo.voice.ustats")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))
            exclude(dependency("com.google.guava:.*"))

            exclude("README.md")

            if (platform.isForge) {
                exclude("fabric.mod.json")
            } else {
                exclude("plasmovoice-forge.mixins.json")
                exclude("pack.mcmeta")
                exclude("META-INF/mods.toml")
                exclude("DebugProbesKt.bin")
            }
        }
    }

    remapJar {
        dependsOn(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    build {
        doLast {
            remapJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${remapJar.get().archiveFile.get().asFile.name}"), true)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "${platform.loaderStr}-${platform.mcVersionStr}"
        }
    }
}

data class VersionInfo(
    val forgeVersion: String,
    val mcVersions: List<String>
) {

    // "${mcVersions}" -> "[1.20,1.20.1]"
    val forgeMcVersions
        get() = "[${mcVersions.joinToString(",")}]"

    // ["${mcVersions}"] -> ["1.20", "1.20.1"]
    val fabricMcVersions
        get() = mcVersions.joinToString("\", \"")
}

fun readVersionInfo(): VersionInfo = Toml()
    .read(file("../versions.toml"))
    .getTable(platform.mcVersion.toString())?.let {
        it.to(VersionInfo::class.java)
    } ?: throw GradleException("Unsupported version ${platform.mcVersion}")
