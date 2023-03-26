val mavenGroup: String by rootProject

plugins {
    id("su.plo.crowdin.plugin")
}

group = "$mavenGroup.server"

dependencies {
    api(project(":api:server"))
    api(project(":server-common"))

    compileOnly(rootProject.libs.netty)
}

plasmoCrowdin {
    projectId = "plasmo-voice"
    sourceFileName = "server/server.toml"
    createList = true
}

tasks {
    processResources {
        dependsOn(plasmoCrowdinDownload)
    }
}
