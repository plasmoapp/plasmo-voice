import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import gg.essential.gradle.util.noServerRunConfigs

val mavenGroup: String by rootProject

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
}

group = "$mavenGroup.client"
base.archivesName.set("plasmovoice-${platform.loaderStr}-${platform.mcVersionStr}")

loom.noServerRunConfigs()

if (platform.isForge) {
    loom.forge.apply {
        mixinConfig(
            "plasmovoice.mixins.json",
            "plasmovoice-forge.mixins.json"
        )
    }
}

loom.runs {
    getByName("client") {
        programArgs("--username", "GNOME__")
        property("plasmovoice.alpha.disableversioncheck", "true")
        property("universalcraft.shader.legacy.debug", "true")
    }
}


val common by configurations.creating
configurations.compileClasspath { extendsFrom(common) }
configurations.runtimeClasspath { extendsFrom(common) }

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    compileOnly(rootProject.libs.netty)
//    compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")

    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11902 -> "0.73.2+1.19.2"
            11903 -> "0.73.2+1.19.3"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
        "include"(modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)
        "include"("net.fabricmc:fabric-language-kotlin:1.9.1+kotlin.1.8.10")
    }

    modApi("gg.essential:universalcraft-$platform:254") {
        exclude(group = "org.jetbrains.kotlin")
    }

    if (platform.isForge) {
        common("gg.essential:universalcraft-$platform:254") {
            exclude(group = "org.jetbrains.kotlin")
        }
    } else {
        "include"("gg.essential:universalcraft-$platform:254") {
            exclude(group = "org.jetbrains.kotlin")
        }
    }

    val includedProjects = listOf(
        ":api:common",
        ":api:client",
        ":api:server-common",
        ":api:server",
        ":server:common",
        ":server-common",
        ":common",
        ":protocol"
    )

    includedProjects.forEach {
        implementation(project(it))
        common(project(it)) {
            isTransitive = false
        }
    }

    common(rootProject.libs.opus)
    common(rootProject.libs.config)
    common(rootProject.libs.rnnoise)

    if (platform.isForge) {
        common(rootProject.libs.guice)
    } else {
        "include"(rootProject.libs.guice)
    }
}

tasks {
    jar {
        mergePlatformSpecifics()

        if (platform.mcVersion >= 11400) {
            excludeKotlinDefaultImpls()
        }
    }

    shadowJar {
        configurations = listOf(common)

        if (platform.isForge) {
            relocate("gg.essential.universal", "su.plo.universal")
        }

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))
            exclude(dependency("com.google.guava:.*"))
            exclude(dependency("com.google.code.findbugs:.*"))
            exclude(dependency("com.google.errorprone:.*"))
            exclude(dependency("com.google.j2objc:.*"))
            exclude(dependency("org.checkerframework:.*"))

            exclude("README.md")
        }
    }

    remapJar {
        dependsOn(common)
        input.set(shadowJar.get().archiveFile)
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
            remapJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${remapJar.get().archiveFile.get().asFile.name}"), true)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "${platform.loaderStr}-${platform.mcVersionStr}"
        }
    }
}
//dependencies {
//    implementation(project(":api:common"))
//    implementation(project(":api:client"))
//
//    implementation(project(":common"))
//    implementation(project(":protocol"))
//
//    compileOnly(rootProject.libs.netty)
//    implementation(rootProject.libs.config)
//    implementation(rootProject.libs.rnnoise)
//}

//apiValidation {
//    ignoredProjects.add(":client-common")
//}
