import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

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

    implementation(project(":common")) {
        isTransitive = false
    }
    project.configurations.getByName("developmentFabric")(project(":common")) {
        isTransitive = false
    }
    "shadowCommon"(project(":common", "transformProductionFabric")) {
        isTransitive = false
    }

    modApi("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    // Fabric API jar-in-jar
    include("net.fabricmc.fabric-api:fabric-api-base:0.3.0+a02b446318")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-command-api-v1:1.1.3+5ab9934c18")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-key-binding-api-v1:1.0.4+cbda931818")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:1.4.4+a02b446318")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-networking-api-v1:1.0.13+cbda931818")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-rendering-v1:1.9.0+7931163218")?.let { modImplementation(it) }
    include("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.4.8+a00e834b18")?.let { modImplementation(it) }

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
    java {
        withSourcesJar()
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf(
                "version" to modVersion,
                "loader_version" to fabricLoaderVersion,
                "fabric_version" to fabricVersion
            ))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.getByName("shadowCommon"))

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