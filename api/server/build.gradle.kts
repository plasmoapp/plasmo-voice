dependencies {
    api(project(":api:common"))
    api(project(":api:server-proxy-common"))

    api("su.plo.slib:api-server:${libs.versions.slib.get()}")
}
