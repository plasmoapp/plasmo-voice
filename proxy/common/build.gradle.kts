plugins {
    id("su.plo.voice.maven-publish")
}

group = "$group.proxy"

dependencies {
    api(project(":api:proxy"))
    api(project(":server-proxy-common"))

    implementation(libs.hkdf)

    compileOnly(libs.netty)
}
