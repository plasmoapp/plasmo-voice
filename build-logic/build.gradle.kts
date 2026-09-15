plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.shadow)
    implementation(libs.config)
    implementation(libs.asm)
    // override dokka's serialization version
    implementation(libs.kotlinx.serialization.core)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.plasmoverse.com/releases")
}
