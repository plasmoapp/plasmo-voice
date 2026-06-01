import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.noServerRunConfigs
import net.fabricmc.loom.task.RemapJarTask
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

val mcVersionsRange = project.property("mod.minecraft_versions") as String
val forgeVersionRange = project.findProperty("mod.forge_version") as String? ?: ""
val neoForgeVersionRange = project.findProperty("mod.neoforge_version") as String? ?: ""

fun slibArtifact(): String {
    val mcSlug = project.findProperty("deps.slib") as String? ?: platform.mcVersionStr
    return "$mcSlug-${platform.loaderStr}"
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
}

dependencies {
    compileOnly(libs.netty)
    implementation(libs.rnnoise.jni)
    implementation(libs.opus.jni)
    implementation(libs.opus.concentus)

    if (platform.isFabric) {
        val fabricApiVersion = project.property("deps.fabric_api") as String
        val fabricPermissionsApiVersion = project.property("deps.fabric_permissions_api") as String
        val clothConfigVersion = project.property("deps.cloth_config") as String
        val modMenuVersion = project.property("deps.modmenu") as String

        fun fabricApiModules(vararg module: String) {
            module.forEach {
                modImplementation(fabricApi.module("fabric-$it", fabricApiVersion))
            }
        }

        fabricApiModules("rendering-v1", "networking-api-v1", "lifecycle-events-v1")

        if (platform.mcVersion < 260100) {
            fabricApiModules("key-binding-api-v1")
        } else {
            fabricApiModules("key-mapping-api-v1")
        }

        val coreProjectFile = project.file("../mainProject")
        val coreProject = coreProjectFile.readText().trim()
        if (coreProject == project.name) {
            modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
        }

        val fabricPermissionsApi = "me.lucko:fabric-permissions-api:$fabricPermissionsApiVersion"
        "include"(fabricPermissionsApi)
        modImplementation(fabricPermissionsApi) {
            isTransitive = false
        }

        modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
            exclude("net.fabricmc.fabric-api")
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
        expandMatching(
            listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml"),
            "version" to version,
            "neoForgeVersion" to neoForgeVersionRange,
            "forgeVersion" to forgeVersionRange,
            "mcVersions" to mcVersionsRange,
            "mixins" to mixins.joinToString("\n[[mixins]]\nconfig=") { "\"$it\"" }.removeSurrounding("\""),
        )

        expandMatching(
            listOf("fabric.mod.json"),
            "version" to version,
            "fabricDependencyName" to if (platform.mcVersion >= 260100) "fabric-api" else "fabric",
            "mcVersions" to mcVersionsRange,
            "mixins" to mixins.joinToString(", ") { "\"$it\"" }.removeSurrounding("\""),
        )

        if (isMainProject) {
            dependsOn("crowdinDownload")
        } else {
            dependsOn(mainProject.tasks.findByName("crowdinDownload") as Any)
        }
    }

    if (isMainProject) {
        findByName("preprocessResources")?.dependsOn("crowdinDownload")
    } else {
        findByName("preprocessResources")?.dependsOn(mainProject.tasks.findByName("crowdinDownload") as Any)
    }

    jar {
        mergePlatformSpecifics()
        excludeKotlinDefaultImpls()
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        if (platform.mcVersion >= 260100) {
            exclude("fabric.mod.json")

            rootSpec.from(zipTree(jar.get().archiveFile)) {
                include("fabric.mod.json")
                include("META-INF/jars/**")
            }
        }

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

    if (platform.mcVersion < 260100) {
        val remapJar = named<RemapJarTask>("remapJar") {
            dependsOn(shadowJar)
            inputFile.set(shadowJar.get().archiveFile)
            archiveClassifier.set("remapped")
        }

        named("copyJarToRootProject") {
            dependsOn(remapJar)
        }
    } else {
        named("copyJarToRootProject") {
            dependsOn(shadowJar)
        }
    }
}
