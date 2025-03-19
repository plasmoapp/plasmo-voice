dependencies {
    api(project(":api:common"))
    api(project(":api:server-proxy-common"))

    api("com.github.PadowYT2.mc-slib:api-server:${libs.versions.slib.get()}")
}
