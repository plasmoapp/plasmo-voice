import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.modrinth.minotaur.TaskModrinthUpload
import com.modrinth.minotaur.request.VersionType
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

val minecraftVersion: String by rootProject
val forgeVersion: String by rootProject

val curseProjectId: String by rootProject
val curseFabricRelease: String by rootProject
val curseDisplayVersion: String by rootProject
val curseSupportedVersions: String by rootProject

val modrinthVersionType: String by rootProject
val modrinthSupportedVersions: String by rootProject
val modrinthProjectId: String by rootProject

configurations {
    create("shadowCommon")
}

configure<LoomGradleExtensionAPI> {
    forge.apply {
        mixinConfig(
            "plasmovoice-common.mixins.json",
            "plasmovoice.mixins.json"
        )
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

dependencies {
    "forge"("net.minecraftforge:forge:${forgeVersion}")

    compileOnly(project(":common", "dev")) {
        isTransitive = false
    }
    project.configurations.getByName("developmentForge")(project(":common", "dev")) {
        isTransitive = false
    }
    "shadowCommon"(project(":common", "transformProductionForge")) {
        isTransitive = false
    }

    // YAML for server config
    implementation("org.yaml:snakeyaml:1.29")
    "shadowCommon"("org.yaml:snakeyaml:1.29")

    // Plasmo Voice protocol
    implementation("su.plo.voice:common:1.0.0")
    "shadowCommon"("su.plo.voice:common:1.0.0")

    // Opus
    implementation("su.plo.voice:opus:1.1.2")
    "shadowCommon"("su.plo.voice:opus:1.1.2")

    // RNNoise
    implementation("su.plo.voice:rnnoise:1.0.0")
    "shadowCommon"("su.plo.voice:rnnoise:1.0.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

repositories {
    maven {
        url = uri("https://repo.plo.su")
    }
    mavenCentral()
    mavenLocal()
}

tasks {
    jar {
        classifier = "dev"
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mutableMapOf(
                "version" to project.version
            ))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        classifier = "dev-shadow"

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }
    }

    remapJar {
        dependsOn(getByName<ShadowJar>("shadowJar"))
        input.set(shadowJar.get().archiveFile)
        archiveBaseName.set("plasmovoice-forge-${minecraftVersion}")
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
            // copy artifact to root project build/libs
            remapJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/" + remapJar.get().archiveFile.get().asFile.name), true)
        }
    }
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

tasks.register<TaskModrinthUpload>("publishModrinth") {
    token = if (file("${rootDir}/modrinth_key.txt").exists()) {
        file("${rootDir}/modrinth_key.txt").readText()
    } else {
        ""
    }

    projectId = modrinthProjectId

    versionNumber = "forge-$curseDisplayVersion-$version"
    versionName = "[Forge ${curseDisplayVersion}] Plasmo Voice $version"
    versionType = VersionType.valueOf(modrinthVersionType)

    modrinthSupportedVersions.split(",").forEach {
        addGameVersion(it)
    }
    changelog = file("${rootDir}/changelog.md").readText()
    addLoader("forge")
    uploadFile = file("${project.buildDir}/libs/${remapJar.archiveBaseName.get()}-${version}.jar")
}


curseforge {
    apiKey = if (file("${rootDir}/curseforge_key.txt").exists()) {
        file("${rootDir}/curseforge_key.txt").readText()
    } else {
        ""
    }

    project(closureOf<CurseProject> {
        id = curseProjectId
        changelog = file("${rootDir}/changelog.txt")
        releaseType = curseFabricRelease
        curseSupportedVersions.split(",").forEach {
            addGameVersion(it)
        }
        addGameVersion("Forge")

        mainArtifact(
            file("${project.buildDir}/libs/${remapJar.archiveBaseName.get()}-${version}.jar"),
            closureOf<CurseArtifact> {
                displayName = "[Forge ${curseDisplayVersion}] Plasmo Voice $version"

                relations(closureOf<CurseRelation> {
                    optionalDependency("sound-physics-remastered")
                })
            })
        afterEvaluate {
            uploadTask.dependsOn(remapJar)
        }
    })
}
