dependencies {
    api(project(":api:common"))
    api(project(":api:server-proxy-common"))

    api("su.plo.slib:api-proxy:${libs.versions.slib.get()}")
}
