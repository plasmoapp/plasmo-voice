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

    configurations {
        create("shadowCommon")
    }

    dependencies {
        "minecraft"("com.mojang:minecraft:${minecraftVersion}")
        mappingsDependency?.let { "mappings"(it) }

        val includedProjects = listOf(
            ":api:common",
            ":api:client",
            ":api:server",
            ":client:common",
            ":server:common",
            ":common",
            ":protocol"
        )

        includedProjects.forEach {
            implementation(project(it))
            "shadowCommon"(project(it))
        }

        compileOnly(rootProject.libs.config)
    }
}

allprojects {
    tasks {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
