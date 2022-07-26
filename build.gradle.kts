// Version
val targetJavaVersion: String by rootProject
val mavenGroup: String by rootProject
val version: String by rootProject

plugins {
    java
    idea
    `maven-publish`
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "maven-publish")

    group = mavenGroup
    version = version

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
}

allprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }
}
