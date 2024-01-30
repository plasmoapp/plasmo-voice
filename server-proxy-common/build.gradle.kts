plugins {
    id("su.plo.voice.maven-publish")
}

dependencies {
    api(project(":api:server-proxy-common"))
    api(project(":common"))

    api(libs.crowdin)

    compileOnly(libs.netty)
    compileOnly(libs.luckperms)
}
