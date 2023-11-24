package su.plo.voice

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        relocate("com.google.inject", "su.plo.voice.libs.google.inject")
        relocate("org.aopalliance", "su.plo.voice.libs.aopalliance")
        relocate("javax.inject", "su.plo.voice.libs.javax.inject")
    }
}
