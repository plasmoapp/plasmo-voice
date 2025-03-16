dependencies {
    api(project(":api:common"))
    api(project(":api:server-proxy-common"))

    api("com.github.PadowYT2.mc-slib:api-proxy:${libs.versions.slib.get()}")
}
