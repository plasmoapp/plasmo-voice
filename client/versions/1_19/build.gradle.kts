import net.fabricmc.loom.api.LoomGradleExtensionAPI

val mavenGroup: String by rootProject

val minecraftVersion: String by project

val fabricLoaderVersion: String by project

plugins {
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.architectury.loom) apply(false)
}

dependencies {
    implementation(project(":client:common"))
}

architectury {
    minecraft = minecraftVersion
}

subprojects {
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")

    group = "$mavenGroup.client"

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

        implementation(project(":api:common"))
        implementation(project(":api:client"))

        implementation(project(":client:common"))

        implementation(project(":common"))
    }
}

allprojects {
    tasks {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
