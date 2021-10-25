import net.fabricmc.loom.LoomGradleExtension

val minecraftVersion: String by rootProject

plugins {
    java
    id("architectury-plugin") version("3.3-SNAPSHOT")
    id("dev.architectury.loom") version("0.7.4-SNAPSHOT") apply(false)
    id("com.github.johnrengelman.shadow") version("7.0.0") apply(false)
    id("com.matthewprenger.cursegradle") version("1.4.0") apply(false)
}

architectury {
    minecraft = minecraftVersion
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    configure<LoomGradleExtension> {
        silentMojangMappingsLicense()
        mixinConfig("plasmovoice.mixins.json")
        useFabricMixin = true
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.matthewprenger.cursegradle")

//    java { toolchain { languageVersion.set(JavaLanguageVersion.of(16)) } }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(16)
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }
}