import su.plo.voice.extension.excludeKotlin
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

    implementation(libs.micrometer.core)
    implementation(libs.micrometer.prometheus)

    implementation(libs.http4k.core) {
        excludeKotlin()
    }
    implementation(libs.http4k.jetty) {
        excludeKotlin()
    }

    testImplementation(libs.mockito)
    testImplementation(libs.netty)
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
