// Version
val targetJavaVersion: String by rootProject
val mavenGroup: String by rootProject
val version: String by rootProject

plugins {
    java
    idea
    `maven-publish`
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")

    group = mavenGroup
    version = version

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    repositories {
        mavenLocal()
    }

    dependencies {
        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
    }
}
