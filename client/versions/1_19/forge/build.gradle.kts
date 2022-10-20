import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI

// todo: придумать как использовать из gradle.properties. Сейчас не получается использовать, потому что это не rootProject
val minecraftVersion = "1.19.2"
val forgeLoaderVersion = "43"
val forgeVersion = "1.19.2-43.1.1"

configure<LoomGradleExtensionAPI> {
    forge.apply {
        mixinConfig(
            "plasmovoice-common.mixins.json",
            "plasmovoice-forge.mixins.json"
        )
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

//configurations {
//    create("common")
//}

dependencies {
    "forge"("net.minecraftforge:forge:${forgeVersion}")

    val includedProjects = listOf(
        ":api:common",
        ":api:client",
        ":api:server",
        ":client:common",
        ":server:common",
        ":common",
        ":protocol"
    )

    forgeRuntimeLibrary(rootProject.libs.config)
    forgeRuntimeLibrary(rootProject.libs.opus) {
        isTransitive = false
    }
    forgeRuntimeLibrary(rootProject.libs.rnnoise) {
        isTransitive = false
    }
    includedProjects.forEach {
        forgeRuntimeLibrary(project(it)) {
            isTransitive = false
        }
    }

    compileOnly(project(":client:versions:1_19:common", "namedElements")) {
        isTransitive = false
    }
    "developmentForge"(project(":client:versions:1_19:common", "namedElements")) {
        isTransitive = false
    }
    "shadowCommon"(project(":client:versions:1_19:common", "transformProductionForge")) {
        isTransitive = false
    }
}

tasks {
    jar {
        archiveClassifier.set("dev")
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mutableMapOf(
                "version" to version,
                "minecraft_version" to minecraftVersion,
                "loader_version" to forgeLoaderVersion,
                "forge_version" to forgeVersion
            ))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }
    }

    remapJar {
        dependsOn(getByName<ShadowJar>("shadowJar"))
        inputFile.set(shadowJar.get().archiveFile)
        archiveBaseName.set("plasmovoice-forge-${minecraftVersion}")
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
        }
    }
}
