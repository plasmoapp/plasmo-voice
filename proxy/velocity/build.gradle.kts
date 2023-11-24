val velocityVersion: String by project

plugins {
    id("su.plo.voice.plugin.relocate-kotlin")
    id("kotlin-kapt")
}

group = "$group.velocity"

dependencies {
    compileOnly(libs.velocity)
    kapt(libs.velocity)
    compileOnly(libs.versions.bstats.map { "org.bstats:bstats-velocity:$it" })
    compileOnly("su.plo.slib:velocity:${libs.versions.slib.get()}")

    compileOnly(project(":proxy:common"))
    compileOnly(libs.netty)

    // shadow projects
    listOf(
        project(":api:common"),
        project(":api:proxy"),
        project(":api:server-common"),
        project(":proxy:common"),
        project(":server-common"),
        project(":common"),
        project(":protocol")
    ).forEach {
        shadow(it) {
            isTransitive = false
        }
    }
    // shadow external deps
    shadow(kotlin("stdlib-jdk8"))
    shadow(libs.kotlinx.coroutines)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.json)

    shadow(libs.opus.concentus)
    shadow(libs.config)
    shadow(libs.crowdin) {
        isTransitive = false
    }
    shadow(libs.versions.bstats.map { "org.bstats:bstats-velocity:$it" })
    shadow("su.plo.slib:velocity:${libs.versions.slib.get()}") {
        isTransitive = false
    }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveBaseName.set("PlasmoVoice-Velocity")
        archiveAppendix.set("")
        archiveClassifier.set("")

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("org.bstats", "su.plo.voice.libs.bstats")

        relocate("org.concentus", "su.plo.voice.libs.concentus")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains:annotations"))

            exclude("su/plo/opus/*")
            exclude("natives/opus/**/*")

            exclude("DebugProbesKt.bin")
            exclude("_COROUTINE/**")
            exclude("META-INF/**")
        }
    }

    build {
        dependsOn.add(shadowJar)

        doLast {
            shadowJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${shadowJar.get().archiveFile.get().asFile.name}"), true)
        }
    }

    jar {
        enabled = false
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
