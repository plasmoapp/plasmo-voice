import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.noServerRunConfigs
import su.plo.config.toml.Toml
import su.plo.crowdin.CrowdinParams
import su.plo.voice.extension.expandMatching
import su.plo.voice.extension.slibPlatform
import java.net.URI

val mainProject = project(":client:${file("../mainProject").readText().trim()}")
val isMainProject = project == mainProject

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("su.plo.voice.shadow")
}

if (isMainProject) {
    apply(plugin = "su.plo.crowdin.plugin")
}

group = "$group.client"
base.archivesName.set("plasmovoice-${platform.loaderStr}-${platform.mcVersionStr}")

loom.noServerRunConfigs()

val mixins = mutableListOf("plasmovoice.mixins.json", "slib.mixins.json")
if (platform.mcVersion >= 12102) {
    mixins.add("plasmovoice-1.21.2.mixins.json")
}

if (platform.mcVersion >= 12106) {
    mixins.add("plasmovoice-1.21.6.mixins.json")
}

if (platform.mcVersion in 12106..12108) {
    mixins.add("plasmovoice-1.21.6-rendertype.mixins.json")
}

if (platform.mcVersion >= 12109) {
    mixins.add("plasmovoice-1.21.9.mixins.json")
}

if (platform.isForge) {
    if (platform.mcVersion < 12002) {
        mixins.add("slib-forge.mixins.json")
    }

    if (platform.mcVersion >= 12100) {
        mixins.add("plasmovoice-forge.mixins.json")
    }

    loom.forge.apply {
        mixins.forEach(::mixinConfig)
    }
}

if (platform.mcVersion >= 12111) {
    mixins.add("plasmovoice-1.21.11.mixins.json")
}

loom {
    runs {
        getByName("client") {
            programArgs("--username", "GNOME__")
            property("plasmovoice.debug", "true")
            property("universalcraft.shader.legacy.debug", "true")
        }
    }
}

if (isMainProject) {
    extensions.getByType<CrowdinParams>().apply {
        url = URI.create("https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/pv.zip").toURL()
        sourceFileName = "client.json"
        resourceDir = "assets/plasmovoice/lang"
        outputDir = layout.projectDirectory.file("../build/generated/sources/crowdin").asFile
    }
}

val shadowCommon by configurations.creating

fun slibArtifact(): String {
    val minecraftVersion = when (platform.mcVersion) {
        11904 -> "1.19.3"
        12103, 12104 -> "1.21.2"
        else -> platform.mcVersionStr
    }

    return "${minecraftVersion}-${platform.loaderStr}"
}

repositories {
    mavenLocal()

    maven("https://repo.plasmoverse.com/snapshots")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.nucleoid.xyz/")

    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven {
        name = "Maven for PR #2815" // https://github.com/neoforged/NeoForge/pull/2815
        url = uri("https://prmaven.neoforged.net/NeoForge/pr2815")
        content {
            includeModule("net.neoforged", "neoforge")
            includeModule("net.neoforged", "testframework")
        }
    }
}

dependencies {
    compileOnly(libs.netty)
    implementation(libs.rnnoise.jni)
    implementation(libs.opus.jni)
    implementation(libs.opus.concentus)

    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11605 -> "0.42.0+1.16"
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11902 -> "0.73.2+1.19.2"
            11903 -> "0.76.1+1.19.3"
            11904 -> "0.87.1+1.19.4"
            12001 -> "0.84.0+1.20.1"
            12004 -> "0.95.4+1.20.4"
            12006 -> "0.97.7+1.20.6"
            12100 -> "0.100.4+1.21"
            12103 -> "0.110.0+1.21.3"
            12104 -> "0.110.5+1.21.4"
            12105 -> "0.119.5+1.21.5"
            12106 -> "0.127.0+1.21.6"
            12109 -> "0.133.14+1.21.9"
            12111 -> "0.139.1+1.21.11"
            else -> throw GradleException("Unsupported platform $platform")
        }

        fun fabricApiModules(vararg module: String) {
            module.forEach {
                modImplementation(fabricApi.module("fabric-$it", fabricApiVersion))
            }
        }

        fabricApiModules("rendering-v1", "networking-api-v1", "lifecycle-events-v1", "key-binding-api-v1")

        val coreProjectFile = project.file("../mainProject")
        val coreProject = coreProjectFile.readText().trim()
        if (coreProject == project.name) {
            modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        }

        val fabricPermissionsApi =
            if (platform.mcVersion >= 12106) {
                "me.lucko:fabric-permissions-api:0.4.1"
            } else if (platform.mcVersion >= 12102) {
                "me.lucko:fabric-permissions-api:0.3.3"
            } else {
                "me.lucko:fabric-permissions-api:0.3.1"
            }
        "include"(fabricPermissionsApi)
        modImplementation(fabricPermissionsApi) {
            isTransitive = false
        }

        // build times go _/

        val clothConfigVersion = when (platform.mcVersion) {
            11605 -> "4.17.101"
            11701 -> "5.3.63"
            11802 -> "6.5.102"
            11902 -> "8.3.134"
            11903 -> "9.1.104"
            11904 -> "10.1.135"
            12001 -> "11.1.136"
            12004 -> "13.0.138"
            12100 -> "15.0.140"
            12103 -> "16.0.143"
            12104,
            12105,
            12106,
            12109,
            12111 -> "17.0.144"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
            exclude("net.fabricmc.fabric-api")
        }

        val modMenuVersion = when (platform.mcVersion) {
            11605 -> "1.16.23"
            11701 -> "2.0.17"
            11802 -> "3.2.5"
            11902 -> "4.1.2"
            11903 -> "5.1.0"
            11904 -> "6.3.1"
            12001 -> "7.2.2"
            12004 -> "9.2.0"
            12100 -> "11.0.3"
            12103 -> "12.0.0"
            12104,
            12105,
            12106,
            12109,
            12111 -> "13.0.2"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("maven.modrinth:modmenu:$modMenuVersion")

        if (platform.mcVersion < 12105) {
            modCompileOnly("maven.modrinth:vulkanmod:0.5.5-fabric,1.21.1")
        }
    }

    val includedProjects = listOf(
        ":api:common",
        ":api:client",
        ":api:server-proxy-common",
        ":api:server",
        ":server:common",
        ":server-proxy-common",
        ":common",
        ":protocol"
    )

    includedProjects.forEach {
        implementation(project(it))
        shadowCommon(project(it))
    }

    shadowCommon(libs.rnnoise.jni)

    // slib
    if (platform.isForge && platform.mcVersion >= 12100) {
        slibPlatform(
            slibArtifact(),
            libs.versions.slib.get(),
            ::api
        ) { name, action -> shadowCommon(name) { action.execute(this) } }
    } else {
        slibPlatform(
            slibArtifact(),
            libs.versions.slib.get(),
            { module, action ->
                modApi(module) {
                    isTransitive = false
                    action.execute(this)
                }
            }
        ) { name, action -> shadowCommon(name) { action.execute(this) } }
    }
}

tasks {
    java {
        withSourcesJar()
    }

    processResources {
        val versionInfo = readVersionInfo()

        expandMatching(
            listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml"),
            "version" to version,
            "neoForgeVersion" to versionInfo.neoForgeVersion,
            "forgeVersion" to versionInfo.forgeVersion,
            "mcVersions" to versionInfo.forgeMcVersions,
            "mixins" to mixins.joinToString("\n[[mixins]]\nconfig=") { "\"$it\"" }.removeSurrounding("\""),
        )

        expandMatching(
            listOf("fabric.mod.json"),
            "version" to version,
            "mcVersions" to versionInfo.fabricMcVersions,
            "mixins" to mixins.joinToString(", ") { "\"$it\"" }.removeSurrounding("\""),
        )

        if (isMainProject) {
            dependsOn("crowdinDownload")
        } else {
            dependsOn(mainProject.tasks.findByName("crowdinDownload"))
        }
    }

    if (isMainProject) {
        findByName("preprocessResources")?.dependsOn("crowdinDownload")
    } else {
        findByName("preprocessResources")?.dependsOn(mainProject.tasks.findByName("crowdinDownload"))
    }

    jar {
        mergePlatformSpecifics()
        excludeKotlinDefaultImpls()
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        dependencies {
            relocate("gg.essential.universal", "su.plo.voice.universal")

            if (platform.mcVersion < 11700 || (platform.isForge && platform.mcVersion < 11800)) {
                exclude(dependency("org.apache.logging.log4j:log4j-api"))
                exclude(dependency("org.apache.logging.log4j:log4j-core"))

                relocate("org.apache.logging.slf4j", "su.plo.voice.libs.org.apache.logging.slf4j")
                relocate("org.slf4j", "su.plo.voice.libs.org.slf4j")
            } else {
                exclude(dependency("org.slf4j:slf4j-api"))
            }

            if (platform.mcVersion < 12102) {
                exclude("plasmovoice-1.21.2.mixins.json")
            }

            if (platform.mcVersion < 12106) {
                exclude("plasmovoice-1.21.6.mixins.json")
            }

            if (platform.mcVersion >= 12106) {
                exclude("assets/plasmovoice/shaders/position_tex_solid_color.*")
            } else {
                exclude("assets/plasmovoice/shaders/position_tex_solid_color_1_21_6.*")
            }

            if (platform.mcVersion >= 11700) {
                exclude("assets/plasmovoice/shaders/position_tex_solid_color_1_16.*")
            }

            if (platform.isForge) {
                exclude("fabric.mod.json")
                exclude("META-INF/neoforge.mods.toml")
            } else if (platform.isNeoForge) {
                exclude("fabric.mod.json")
                exclude("META-INF/mods.toml")
            } else {
                exclude("pack.mcmeta")
                exclude("META-INF/mods.toml")
                exclude("META-INF/neoforge.mods.toml")
            }
        }
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("remapped")
    }

    named("copyJarToRootProject") {
        dependsOn(remapJar)
    }
}

data class VersionInfo(
    val neoForgeVersion: String,
    val forgeVersion: String,
    val mcVersions: String
) {

    val forgeMcVersions: String
        get() {
            val split = mcVersions.split(" ")

            fun versionBounds(version: String): Triple<String, String, String>? =
                when {
                    version.startsWith(">=") -> Triple(
                        "[", version.substringAfter(">="), ",)"
                    )
                    version.startsWith(">") -> Triple(
                        "(", version.substringAfter(">"), ",)"
                    )
                    version.startsWith("<=") -> Triple(
                        "(,", version.substringAfter("<="), "]"
                    )
                    version.startsWith("<") -> Triple(
                        "(,", version.substringAfter("<"), ")"
                    )
                    else -> null
                }

            if (split.size == 1) {
                val version = split.first()
                val bounds = versionBounds(version)

                return if (bounds == null) {
                    "[$version]"
                } else {
                    "${bounds.first}${bounds.second}${bounds.third}"
                }
            } else if (split.size == 2) {
                val bounds = split.map { versionBounds(it)!! }
                val first = bounds[0]
                val second = bounds[1]

                return "${first.first}${first.second},${second.second}${second.third}"
            } else {
                throw IllegalStateException("Invalid version")
            }
        }

    val fabricMcVersions
        get() = mcVersions
}

fun readVersionInfo(): VersionInfo = Toml()
    .read(file("../versions.toml"))
    .getTable(platform.mcVersion.toString())?.let {
        it.to(VersionInfo::class.java)
    } ?: throw GradleException("Unsupported version ${platform.mcVersion}")
