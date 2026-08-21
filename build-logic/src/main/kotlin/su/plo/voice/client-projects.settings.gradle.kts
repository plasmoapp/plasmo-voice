package su.plo.voice

import java.io.File

if (providers.gradleProperty("project.client.disable").getOrElse("false") != "true") {
    include("client")
    project(":client").apply {
        projectDir = File(rootDir, "client")
        buildFileName = "root.gradle.kts"
    }

    val clientParents = clientVersions(File(rootDir, "client/versions.json"))
        .associate { it.project to it.parent }

    val requestedClient = providers.gradleProperty("project.client.only").orNull
    val clientVersions = if (requestedClient == null) {
        clientParents.keys
    } else {
        val requested = requestedClient.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        require(requested.isNotEmpty()) { "`project.client.only` does not name any client version" }
        requested.flatMap { version ->
            require(version in clientParents) {
                "unknown client version `$version`, expected one of ${clientParents.keys.sorted()}"
            }
            generateSequence(version) { clientParents[it] }.toList()
        }.toSet()
    }

    clientVersions.forEach {
        include("client:$it")
        project(":client:$it").apply {
            projectDir = File(rootDir, "client/$it")
            buildFileName = "../build.gradle.kts"
        }
    }
}
