import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.RelocationTransform.Companion.registerRelocationAttribute
import gg.essential.gradle.util.noServerRunConfigs
import gg.essential.util.prebundleNow
import su.plo.config.toml.Toml

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
            "plasmovoice-forge.mixins.json",
            "slib.mixins.json",
            "slib-forge.mixins.json"
        )
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

fun uStatsVersion() = libs.versions.ustats.map {
    val minecraftVersion = when (platform.mcVersion) {
        12001 -> "1.20"
        else -> platform.mcVersionStr
    }

    "${minecraftVersion}-${platform.loaderStr}:$it"
}.get()

fun universalCraftVersion() = libs.versions.universalcraft.map {
    val minecraftVersion = when (platform.mcVersion) {
        11605 -> "1.16.2"
        11802 -> "1.18.1"
        else -> platform.mcVersionStr
    }

    "${minecraftVersion}-${platform.loaderStr}:$it"
}.get()

fun slibVersion() = libs.versions.slib.map {
    val minecraftVersion = when (platform.mcVersion) {
        11802, 11902, 11904, 12001 -> "1.19.3"
        else -> platform.mcVersionStr
    }

    "${minecraftVersion}-${platform.loaderStr}:$it"
}.get()

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
}

dependencies {
    compileOnly(libs.netty)
    implementation(libs.rnnoise.jni)

    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11605 -> "0.42.0+1.16"
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11902 -> "0.73.2+1.19.2"
            11903 -> "0.73.2+1.19.3"
            11904 -> "0.87.1+1.19.4"
            12001 -> "0.84.0+1.20.1"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
        "include"("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
    }

    universalCraft("gg.essential:universalcraft-${universalCraftVersion()}") {
        isTransitive = false
    }
    modApi(prebundleNow(universalCraft))
    shadowCommon(prebundleNow(universalCraft))

    "su.plo.ustats:${uStatsVersion()}".also {
        modApi(it) {
            isTransitive = false
        }
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

    // slib
    "su.plo.slib:${slibVersion()}".also {
        modApi(it)
        shadowCommon(it) {
            isTransitive = false
        }
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

    shadowCommon(libs.guice) {
        exclude("com.google.guava")
    }

    if (platform.mcVersion < 11700) {
        shadowCommon(libs.slf4j)
        shadowCommon(libs.slf4j.log4j)
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
        dependsOn(crowdinDownload)
    }

    jar {
        mergePlatformSpecifics()
        excludeKotlinDefaultImpls()
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("su.plo.ustats", "su.plo.voice.libs.ustats")

        relocate("com.google.inject", "su.plo.voice.libs.google.inject")
        relocate("org.aopalliance", "su.plo.voice.libs.aopalliance")
        relocate("javax.inject", "su.plo.voice.libs.javax.inject")

        relocate("org.concentus", "su.plo.voice.libs.concentus")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.jetbrains:annotations"))
            exclude(dependency("com.google.guava:.*"))

            exclude("README.md")
            exclude("DebugProbesKt.bin")
            exclude("META-INF/*.kotlin_module")
            exclude("_COROUTINE/**")

            if (platform.mcVersion >= 11700) {
                exclude(dependency("org.slf4j:slf4j-api"))
            } else {
                exclude(dependency("org.apache.logging.log4j:log4j-api"))
                exclude(dependency("org.apache.logging.log4j:log4j-core"))

                relocate("org.apache.logging.slf4j", "su.plo.voice.libs.org.apache.logging.slf4j")
                relocate("org.slf4j", "su.plo.voice.libs.org.slf4j")
            }

            if (platform.isForge) {
                exclude("fabric.mod.json")
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
