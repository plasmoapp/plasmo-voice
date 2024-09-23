import java.net.URI

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
    url = URI.create("https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/pv.zip").toURL()
    sourceFileName = "server.toml"
    createList = true
}

tasks {
    processResources {
        dependsOn(crowdinDownload)
    }
}
