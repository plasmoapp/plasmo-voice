import net.fabricmc.loom.api.LoomGradleExtensionAPI

val minecraftVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

plugins {
    java
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("dev.architectury.loom") version("0.11.0-SNAPSHOT") apply(false)
    id("com.github.johnrengelman.shadow") version("7.0.0") apply(false)
    id("com.matthewprenger.cursegradle") version("1.4.0") apply(false)
    id("com.modrinth.minotaur") version("2.+") apply(false)
}

architectury {
    minecraft = minecraftVersion
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    var mappingsDependency: Dependency? = null

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()

        mappingsDependency = layered {
            officialMojangMappings()
        }

        launches {
            named("client") {
//                property("fabric.log.level", "debug")
            }
        }
    }

    dependencies {
        "minecraft"("com.mojang:minecraft:${minecraftVersion}")
        mappingsDependency?.let { "mappings"(it) }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "com.github.johnrengelman.shadow")

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

    base {
        archivesBaseName = "plasmovoice"
    }

    project.group = mavenGroup
    project.version = modVersion

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    repositories {
        maven {
            url = uri("https://repo.plo.su")
        }

        mavenCentral()
        mavenLocal()
    }

    java {
        withSourcesJar()
    }
}
