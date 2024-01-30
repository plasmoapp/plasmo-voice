plugins {
    id("org.jetbrains.dokka")
    id("su.plo.voice.maven-publish")
}

dependencies {
    implementation("su.plo.slib:api-common:${libs.versions.slib.get()}")
}

