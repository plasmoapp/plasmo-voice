import gg.essential.gradle.util.setJvmDefault
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.ByteArrayOutputStream

// Version
val targetJavaVersion: String by rootProject

plugins {
    java
    idea
    id("com.github.johnrengelman.shadow")
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.dokka)
    alias(libs.plugins.crowdin) apply(false)

    kotlin("jvm") version(libs.versions.kotlin.get())
    kotlin("plugin.lombok") version(libs.versions.kotlin.get())
    kotlin("kapt") version(libs.versions.kotlin.get())

    id("gg.essential.multi-version.root") apply(false)
}

subprojects {
    if (project.buildFile.name.equals("root.gradle.kts")) return@subprojects

    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-lombok")

    if (properties.containsKey("snapshot")) {
        if (!project.parent?.name.equals("api")) {
            val gitCommitHash: String = ByteArrayOutputStream().use { outputStream ->
                rootProject.exec {
                    commandLine("git")
                        .args("rev-parse", "--verify", "--short", "HEAD")
                    standardOutput = outputStream
                }
                outputStream.toString().trim()
            }.substring(0, 7) // windows moment?
            version = "$version+$gitCommitHash-SNAPSHOT"
        } else {
            version = "$version-SNAPSHOT"
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(rootProject.libs.kotlinx.coroutines)
        implementation(rootProject.libs.kotlinx.coroutines.jdk8)
        implementation(rootProject.libs.kotlinx.json)

        compileOnly(rootProject.libs.guava)
        compileOnly(rootProject.libs.gson)
        compileOnly(rootProject.libs.guice)

        compileOnly(rootProject.libs.slf4j)

        api(rootProject.libs.annotations)
        api(rootProject.libs.lombok)

        annotationProcessor(rootProject.libs.lombok)

        testCompileOnly(rootProject.libs.junit.api)
        testAnnotationProcessor(rootProject.libs.junit.api)
        testRuntimeOnly(rootProject.libs.junit.engine)

        testImplementation(rootProject.libs.guava)
        testImplementation(rootProject.libs.gson)
        testImplementation(rootProject.libs.slf4j)
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }

        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }

        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name()
        }

        compileKotlin {
            setJvmDefault("all")
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.plo.su")
        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io/")
    }
}

tasks {
    jar {
        enabled = false
    }
}
