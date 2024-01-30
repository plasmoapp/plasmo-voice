package su.plo.voice.util

import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

fun Project.copyJarToRootProject(task: Jar) {
    val file = task.archiveFile.get().asFile
    val destinationFile = rootProject.layout.buildDirectory
        .file("libs/${file.name.replace("-all", "")}")
        .get()
        .asFile

    file.copyTo(destinationFile, true)
}
