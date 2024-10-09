import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.noServerRunConfigs
import su.plo.config.toml.Toml
import su.plo.voice.extension.slibPlatform
import su.plo.voice.util.copyJarToRootProject
import java.net.URI

val isMainProject = project.name == file("../mainProject").readText().trim()

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("su.plo.crowdin.plugin")
    id("su.plo.voice.relocate")
}

group = "$group.client"
base.archivesName.set("plasmovoice-${platform.loaderStr}-${platform.mcVersionStr}")

loom.noServerRunConfigs()

if (platform.isForge) {
    loom.forge.apply {
        mixinConfig(
            "plasmovoice.mixins.json",
            "slib.mixins.json"
        )

        if (platform.mcVersion < 12002) {
            mixinConfig("slib-forge.mixins.json")
        }

        if (platform.mcVersion >= 12100) {
            mixinConfig("plasmovoice-forge.mixins.json")
        }
    }
}

loom {
    runs {
        getByName("client") {
            programArgs("--username", "GNOME__")
            property("plasmovoice.alpha.disableversioncheck", "true")
            property("plasmovoice.debug", "true")
            property("universalcraft.shader.legacy.debug", "true")
        }
    }
}

crowdin {
    url = URI.create("https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/pv.zip").toURL()
    sourceFileName = "client.json"
    resourceDir = "assets/plasmovoice/lang"
}

val shadowCommon by configurations.creating

fun slibArtifact(): String {
    val minecraftVersion = when (platform.mcVersion) {
        11904 -> "1.19.3"
        else -> platform.mcVersionStr
    }

    return "${minecraftVersion}-${platform.loaderStr}"
}

repositories {
    maven("https://repo.plasmoverse.com/snapshots")
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
            12102 -> "0.105.3+1.21.2"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

        if (platform.mcVersion >= 12102) {
            // https://github.com/lucko/fabric-permissions-api/pull/26
            "include"("com.github.sakura-ryoko:fabric-permissions-api:b43d33efb8")
        } else {
            "include"("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
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
        shadowCommon(project(it)) {
            isTransitive = false
        }
    }

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
            ::modApi
        ) { name, action -> shadowCommon(name) { action.execute(this) } }
    }

    // kotlin
    shadowCommon(kotlin("stdlib-jdk8"))
    shadowCommon(libs.kotlinx.coroutines)
    shadowCommon(libs.kotlinx.coroutines.jdk8)
    shadowCommon(libs.kotlinx.json)

    shadowCommon(libs.config)
    shadowCommon(libs.opus.jni)
    shadowCommon(libs.opus.concentus)
    shadowCommon(libs.rnnoise.jni)
    shadowCommon(libs.crowdin) {
        isTransitive = false
    }
}

tasks {
    java {
        withSourcesJar()
    }

    getByName<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        val versionInfo = readVersionInfo()

        filesMatching(
            mutableListOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
        ) {
            expand(
                mutableMapOf(
                    "version" to version,
                    "neoForgeVersion" to versionInfo.neoForgeVersion,
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
        dependsOn(crowdinDownload)
    }

    jar {
        mergePlatformSpecifics()
        excludeKotlinDefaultImpls()
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        dependencies {
            exclude(dependency("com.google.guava:.*"))

            exclude("README.md")
            exclude("META-INF/*.kotlin_module")

            relocate("gg.essential.universal", "su.plo.voice.universal")

            if (platform.mcVersion < 11700 || (platform.isForge && platform.mcVersion < 11800)) {
                exclude(dependency("org.apache.logging.log4j:log4j-api"))
                exclude(dependency("org.apache.logging.log4j:log4j-core"))

                relocate("org.apache.logging.slf4j", "su.plo.voice.libs.org.apache.logging.slf4j")
                relocate("org.slf4j", "su.plo.voice.libs.org.slf4j")
            } else {
                exclude(dependency("org.slf4j:slf4j-api"))
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
            }
        }
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    build {
        doLast {
            copyJarToRootProject(remapJar.get())
        }
    }
}

data class VersionInfo(
    val neoForgeVersion: String,
    val forgeVersion: String,
    val mcVersions: List<String>
) {

    // "${mcVersions}" -> "[1.20,1.20.1]"
    val forgeMcVersions
        get() =
            if (mcVersions[0].startsWith(">=")) {
                "[${mcVersions[0].substringAfter(">=")},)"
            } else {
                "[${mcVersions.joinToString(",")}]"
            }

    // ["${mcVersions}"] -> ["1.20", "1.20.1"]
    val fabricMcVersions
        get() = mcVersions.joinToString("\", \"")
}

fun readVersionInfo(): VersionInfo = Toml()
    .read(file("../versions.toml"))
    .getTable(platform.mcVersion.toString())?.let {
        it.to(VersionInfo::class.java)
    } ?: throw GradleException("Unsupported version ${platform.mcVersion}")
