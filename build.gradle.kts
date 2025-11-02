import gg.essential.gradle.util.setJvmDefault

// Version
val targetJavaVersion: String by rootProject

plugins {
    java
    idea
    alias(libs.plugins.dokka)
    alias(libs.plugins.grgit)
    alias(libs.plugins.crowdin) apply(false)

    kotlin("jvm") version(libs.versions.kotlin.get())
    kotlin("plugin.lombok") version(libs.versions.kotlin.get())
    kotlin("kapt") version(libs.versions.kotlin.get())

    id("gg.essential.multi-version.root") apply(false)
}

if (properties.containsKey("snapshot")) {
    val gitCommitHash = grgit.head().abbreviatedId.substring(0, 7)
    version = "$version+$gitCommitHash-SNAPSHOT"
}

subprojects {
    if (project.buildFile.name.equals("root.gradle.kts")) return@subprojects

    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-lombok")

    version = rootProject.version

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(rootProject.libs.kotlinx.coroutines)
        implementation(rootProject.libs.kotlinx.coroutines.jdk8)
        implementation(rootProject.libs.kotlinx.json)

        compileOnly(rootProject.libs.guava)
        compileOnly(rootProject.libs.gson)
        compileOnly(rootProject.libs.fastutil)

        api(rootProject.libs.annotations)
        api(rootProject.libs.lombok)

        annotationProcessor(rootProject.libs.lombok)

        testImplementation(kotlin("test"))
        testImplementation(rootProject.libs.guava)
        testImplementation(rootProject.libs.gson)
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks {
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

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

            compilerOptions {
                freeCompilerArgs.add("-Xcontext-receivers")
            }
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
        maven("https://maven.neoforged.net/releases")
    }
}

tasks {
    jar {
        enabled = false
    }
}
