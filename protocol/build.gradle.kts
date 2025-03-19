plugins {
    id("org.jetbrains.dokka")
    id("su.plo.voice.maven-publish")
}

dependencies {
    implementation("com.github.PadowYT2.mc-slib:api-common:${libs.versions.slib.get()}")
}

