import java.net.URI

plugins {
    id("org.jetbrains.dokka")
    id("su.plo.voice.maven-publish")
}

dokka {
    dokkaSourceSets.configureEach {
        externalDocumentationLinks {
            register("slib") {
                url.set(URI("https://slib.plasmoverse.com/"))
                packageListUrl.set(URI("https://slib.plasmoverse.com/package-list"))
            }
        }
    }
}

dependencies {
    api("it.unimi.dsi:fastutil") { version { prefer(libs.versions.fastutil.get()) } }

    implementation("su.plo.slib:api-common:${libs.versions.slib.get()}")
}
