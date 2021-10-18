import net.fabricmc.loom.api.LoomGradleExtensionAPI

val minecraftVersion: String by rootProject

plugins {
    java
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("dev.architectury.loom") version("0.10.0-SNAPSHOT") apply(false)
    id("com.github.johnrengelman.shadow") version("7.0.0") apply(false)
}

architectury {
    minecraft = minecraftVersion
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "com.github.johnrengelman.shadow")

    tasks.withType<JavaCompile>().configureEach {
        with(options) {
            encoding = "UTF-8"
            release.set(8)
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    java {
        withSourcesJar()
    }
}