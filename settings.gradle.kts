pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Fabric-Forge loom"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "Forge"
            url = uri("https://files.minecraftforge.net/maven/")
        }
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io/")
        }

        gradlePluginPortal()
    }
}

rootProject.name = "PlasmoVoice"

include(
    "common",
    "fabric",
//    "forge"
)