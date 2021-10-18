import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI

val minecraftVersion: String by rootProject
val forgeVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

configurations {
    create("shadowCommon")
}

architectury {
    platformSetupLoomIde()
    forge()
}

configure<LoomGradleExtensionAPI> {
    silentMojangMappingsLicense()

    forge {
        mixinConfig("plasmovoice.mixins.json")
    }
}

base {
    archivesBaseName = "plasmovoice"
}

project.group = mavenGroup
project.version = modVersion

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    "forge"("net.minecraftforge:forge:${forgeVersion}")

    implementation(project(":common", "dev")) {
        isTransitive = false
    }
    project.configurations.getByName("developmentForge")(project(":common", "dev")) {
        isTransitive = false
    }
    "shadowCommon"(project(":common", "transformProductionFabric")) {
        isTransitive = false
    }

    // YAML for server config
    implementation("org.yaml:snakeyaml:1.29")
    "shadowCommon"("org.yaml:snakeyaml:1.29")

    // Plasmo Voice protocol
    implementation("su.plo.voice:common:1.0.0")
    "shadowCommon"("su.plo.voice:common:1.0.0")

    // YAML for server config
    implementation("org.yaml:snakeyaml:1.29")
    "shadowCommon"("org.yaml:snakeyaml:1.29")

    // Opus
    implementation("su.plo.voice:opus:1.1.2-old-jna")
    "shadowCommon"("su.plo.voice:opus:1.1.2-old-jna")

    // RNNoise
    implementation("su.plo.voice:rnnoise:1.0.0-old-jna")
    "shadowCommon"("su.plo.voice:rnnoise:1.0.0-old-jna")

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
        inputs.property("version", modVersion)

        filesMatching("META-INF/mods.toml") {
            expand(mutableMapOf(
                "version" to modVersion
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
        input.set(shadowJar.get().archiveFile)
        dependsOn(getByName<ShadowJar>("shadowJar"))
        archiveBaseName.set("plasmovoice-forge-${minecraftVersion}")
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
        }
    }
}