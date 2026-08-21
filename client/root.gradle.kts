import su.plo.voice.clientVersions

plugins {
    id("gg.essential.multi-version.root")
}

group = "$group.client-root"

val versions = clientVersions(file("versions.json"))

preprocess {
    strictExtraMappings.set(false)

    val nodes = versions
        .filter { it.project in childProjects.keys }
        .associate { it.project to createNode(it.project, it.mcVersion, it.mappings) }

    versions.forEach { version ->
        val node = nodes[version.project] ?: return@forEach
        val parent = version.parent ?: return@forEach
        node.link(nodes.getValue(parent), version.extraMappings?.let(::file))
    }
}
