subprojects {
    group = "$group.api"

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "su.plo.voice.maven-publish")

    dependencies {
        api(rootProject.libs.guava)
        api(rootProject.libs.gson)
        api(rootProject.libs.config)
        api(project(":protocol"))
    }
}

tasks.jar {
    enabled = false
}
