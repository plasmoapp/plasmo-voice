import gg.essential.gradle.util.setJvmDefault
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.ByteArrayOutputStream

// Version
val targetJavaVersion: String by rootProject
val mavenGroup: String by rootProject
val buildVersion: String by rootProject

plugins {
    java
    idea
    alias(libs.plugins.shadow)
    alias(libs.plugins.idea.ext)

    kotlin("jvm") version "1.6.10"
    id("gg.essential.multi-version.root") apply false
}

subprojects {
    if (project.buildFile.name.equals("root.gradle.kts")) return@subprojects

    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "kotlin")

    group = mavenGroup

    if (buildVersion.contains("+") && !project.parent?.name.equals("api")) {
        val gitCommitHash: String = ByteArrayOutputStream().use { outputStream ->
            rootProject.exec {
                commandLine("git")
                    .args("rev-parse", "--verify", "--short", "HEAD")
                standardOutput = outputStream
            }
            outputStream.toString().trim()
        }
        version = "${buildVersion.split("+")[0]}+$gitCommitHash"
    } else {
        version = buildVersion
    }

    dependencies {
        compileOnly(rootProject.libs.annotations)
        compileOnly(rootProject.libs.guava)
        compileOnly(rootProject.libs.gson)
        compileOnly(rootProject.libs.log4j)

        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)

        testCompileOnly(rootProject.libs.junit.api)
        testAnnotationProcessor(rootProject.libs.junit.api)
        testRuntimeOnly(rootProject.libs.junit.engine)

        testImplementation(rootProject.libs.guava)
        testImplementation(rootProject.libs.gson)
        testImplementation(rootProject.libs.log4j)
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks {
        java {
            withJavadocJar()
            withSourcesJar()

            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

            val os: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
            if (os.isMacOsX) {
                toolchain.vendor.set(JvmVendorSpec.AZUL)
            }
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

//        compileKotlin.setJvmDefault("all")

//        jar {
//            archiveClassifier.set("dev")
//        }
//
//        shadowJar {
//            configurations = listOf(project.configurations.shadow.get())
//            archiveClassifier.set("dev-shadow")
//        }
//
//        build {
//            dependsOn.add(shadowJar)
//        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.plo.su")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

tasks {
    build {
        doLast {
            jar.get().archiveFile.get().asFile.delete()
        }
    }
}
