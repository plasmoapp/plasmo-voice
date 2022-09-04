// Version
val targetJavaVersion: String by rootProject
val mavenGroup: String by rootProject
val buildVersion: String by rootProject

plugins {
    java
    idea
    alias(libs.plugins.shadow)
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = mavenGroup
    version = buildVersion

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
        }

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

        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}
