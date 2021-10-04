import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject
val archiveBaseName: String by project

plugins {
    java
    id("com.github.johnrengelman.shadow") version("7.0.0")
    id("fabric-loom") version("0.8-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16

    toolchain { languageVersion.set(JavaLanguageVersion.of(16)) }
}

base {
    archivesBaseName = archiveBaseName
}

project.group = mavenGroup
project.version = modVersion

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(minecraft.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    // Fabric API jar-in-jar
    include("net.fabricmc.fabric-api:fabric-api-base:0.3.0+c88702897d")
    include("net.fabricmc.fabric-api:fabric-networking-api-v1:1.0.14+6eb8b35a88")
    include("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:1.4.4+a02b4463d5")
    include("net.fabricmc.fabric-api:fabric-key-binding-api-v1:1.0.4+a02b4463d5")
    include("net.fabricmc.fabric-api:fabric-rendering-v1:1.6.0+a02b4463d5")
    include("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.4.7+b7ab6121d5")
    include("net.fabricmc.fabric-api:fabric-command-api-v1:1.1.1+bb687600d1")

    // sound physics
    compileOnly("com.soniether:Sound-Physics-Fabric:2.0.serverfix")

    implementation("su.plo.voice:common:1.0.0")
    shadow("su.plo.voice:common:1.0.0")

    implementation("su.plo.voice:opus:1.1.2")
    shadow("su.plo.voice:opus:1.1.2")

    implementation("su.plo.voice:rnnoise:1.0.0")
    shadow("su.plo.voice:rnnoise:1.0.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(16)
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
        val replaces: Map<String, String> = mapOf(
                "version" to modVersion,
                "loader_version" to fabricLoaderVersion,
                "fabric_version" to fabricVersion
        )
        filesMatching("fabric.mod.json") {
            expand(replaces)
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }
    }

    remapJar {
        dependsOn(getByName<ShadowJar>("shadowJar"))
        input.set(shadowJar.get().archiveFile)
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
        }
    }
}