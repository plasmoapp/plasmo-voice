import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.modrinth.minotaur.TaskModrinthUpload
import com.modrinth.minotaur.request.VersionType
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import net.fabricmc.loom.task.RemapJarTask

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

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

architectury {
    platformSetupLoomIde()
    fabric()
}

base {
    archivesBaseName = "plasmovoice"
}

project.group = mavenGroup
project.version = modVersion

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    compileOnly(project(":common", "dev")) {
        isTransitive = false
    }
    project.configurations.getByName("developmentFabric")(project(":common", "dev")) {
        isTransitive = false
    }
    "shadowCommon"(project(":common", "transformProductionFabric")) {
        isTransitive = false
    }

    modApi("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    // Fabric API jar-in-jar
    include("net.fabricmc.fabric-api:fabric-api-base:0.4.1+b4f4f6cd14")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-command-api-v1:1.1.6+3ac43d9514")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-key-binding-api-v1:1.0.8+c8aba2f314")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:1.4.10+c15ca33514")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-networking-api-v1:1.0.18+3ac43d9514")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-rendering-v1:1.10.3+6b21378a14")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.4.11+3ac43d9514")?.let { modImplementation(it) }

    // Plasmo Voice protocol
    implementation("su.plo.voice:common:1.0.0")
    "shadowCommon"("su.plo.voice:common:1.0.0")

    // YAML for server config
    implementation("org.yaml:snakeyaml:1.29")
    "shadowCommon"("org.yaml:snakeyaml:1.29")

    // Opus
    implementation("su.plo.voice:opus:1.1.2")
    "shadowCommon"("su.plo.voice:opus:1.1.2")

    // RNNoise
    implementation("su.plo.voice:rnnoise:1.0.0")
    "shadowCommon"("su.plo.voice:rnnoise:1.0.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
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
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to modVersion,
                    "loader_version" to fabricLoaderVersion,
                    "fabric_version" to fabricVersion
                )
            )
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
        archiveBaseName.set("plasmovoice-fabric-${minecraftVersion}")
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
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

    versionNumber = "fabric-$curseDisplayVersion-$version"
    versionName = "[Fabric ${curseDisplayVersion}] Plasmo Voice $version"
    versionType = VersionType.valueOf(modrinthVersionType)

    modrinthSupportedVersions.split(",").forEach {
        addGameVersion(it)
    }
    changelog = file("${rootDir}/changelog.md").readText()
    addLoader("fabric")
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
        addGameVersion("Fabric")

        mainArtifact(
            file("${project.buildDir}/libs/${remapJar.archiveBaseName.get()}-${version}.jar"),
            closureOf<CurseArtifact> {
                displayName = "[Fabric ${curseDisplayVersion}] Plasmo Voice $version"

                relations(closureOf<CurseRelation> {
                    optionalDependency("sound-physics-fabric")
                    optionalDependency("sound-physics-remastered")
                })
            })
        afterEvaluate {
            uploadTask.dependsOn(remapJar)
        }
    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}