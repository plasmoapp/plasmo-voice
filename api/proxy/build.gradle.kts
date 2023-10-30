dependencies {
    api(project(":api:common"))
    api(project(":api:server-common"))

    api("su.plo.slib:api-proxy:${libs.versions.slib.get()}")
}
