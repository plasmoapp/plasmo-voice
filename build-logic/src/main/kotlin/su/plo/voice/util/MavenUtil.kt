package su.plo.voice.util

import org.gradle.api.Project

fun Project.mavenProjectName(parent: Boolean = true): String =
    if (parent) {
        "${this.parent!!.name}-${this.name}"
    } else {
        this.name
    }
