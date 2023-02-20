plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
}
