val targetJavaVersion: String by rootProject

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":macos:protocol"))
    implementation(kotlin("stdlib-jdk8"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

application {
    mainClass.set("su.plo.voice.mac.probe.MainKt")
}
