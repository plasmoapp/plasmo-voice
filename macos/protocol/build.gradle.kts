import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }

    macosX64()
    macosArm64()

    applyDefaultHierarchyTemplate()
    sourceSets["commonMain"].apply {
        kotlin.setSrcDirs(listOf("src/main/kotlin"))
        dependencies {
            api(libs.kotlinx.msgpack)
            api(libs.kotlinx.io)
        }
    }
    sourceSets["jvmMain"].kotlin.setSrcDirs(listOf("src/jvm/kotlin"))
}
