plugins {
    id("su.plo.crowdin.plugin")
    id("su.plo.voice.maven-publish")
}

group = "$group.server"

dependencies {
    api(project(":api:server"))
    api(project(":server-proxy-common"))

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
