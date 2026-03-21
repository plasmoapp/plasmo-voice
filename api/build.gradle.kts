import org.jetbrains.dokka.gradle.DokkaExtension
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}

subprojects {
    group = "$group.api"

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "su.plo.voice.maven-publish")

    configure<DokkaExtension> {
        modulePath.set(project.name)
        dokkaSourceSets.configureEach {
            externalDocumentationLinks {
                register("slib") {
                    url.set(URI("https://slib.plasmoverse.com/"))
                    packageListUrl.set(URI("https://slib.plasmoverse.com/package-list"))
                }
            }
        }
    }

    dependencies {
        api(rootProject.libs.guava)
        api(rootProject.libs.gson)
        api(rootProject.libs.fastutil)
        api(rootProject.libs.config)
        api(project(":protocol"))
    }
}

dependencies {
    subprojects.forEach { dokka(it) }
}

tasks.jar {
    enabled = false
}
