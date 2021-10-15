import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

base {
    archivesBaseName = "plasmovoice"
}

project.group = mavenGroup
project.version = modVersion

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    "forge"("net.minecraftforge:forge:${forgeVersion}")

    implementation(project(":common")) {
        isTransitive = false
    }
    project.configurations.getByName("developmentForge")(project(":common")) {
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
        inputs.property("version", modVersion)

        filesMatching("META-INF/mods.toml") {
            expand(mutableMapOf(
                "version" to modVersion
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
        archiveBaseName.set("plasmovoice-forge")
    }

//    jar {
//        manifest {
//            attributes(mutableMapOf(
//                "Specification-Title" to "plasmovoice",
//                "Specification-Vendor" to "Plasmo",
//                "Specification-Version" to "1",
//                "Implementation-Title" to "Plasmo Voice",
//                "Implementation-Version" to modVersion,
//                "Implementation-Vendor" to "`Plasmo`",
//                "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
//                "MixinConfigs" to "plasmovoice.mixins.json"
//            ))
//        }
//    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
        }
    }
}