plugins {
    id("su.plo.crowdin.plugin")
}

group = "$group.server"

dependencies {
    api(project(":api:server"))
    api(project(":server-common"))

    compileOnly(libs.netty)
}

crowdin {
    projectId = "plasmo-voice"
    sourceFileName = "server.toml"
    createList = true
}

tasks {
    processResources {
        dependsOn(crowdinDownload)
    }
}
