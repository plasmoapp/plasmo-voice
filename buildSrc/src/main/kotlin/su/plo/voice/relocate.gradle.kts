package su.plo.voice

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        relocate("kotlin", "su.plo.voice.libs.kotlin")
        relocate("kotlinx.coroutines", "su.plo.voice.libs.kotlinx.coroutines")
        relocate("kotlinx.serialization", "su.plo.voice.libs.kotlinx.serialization")

        relocate("su.plo.crowdin", "su.plo.voice.libs.crowdin")
        relocate("su.plo.ustats", "su.plo.voice.libs.ustats")
        relocate("org.bstats", "su.plo.voice.libs.bstats")

        relocate("org.concentus", "su.plo.voice.libs.concentus")

        dependencies {
            exclude(dependency("net.java.dev.jna:jna"))
            exclude(dependency("org.jetbrains:annotations"))

            exclude("DebugProbesKt.bin")
            exclude("_COROUTINE/**")
        }
    }
}
