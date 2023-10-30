group = "$group.proxy-common"

dependencies {
    api(project(":api:proxy"))
    api(project(":server-common"))

    compileOnly(libs.netty)
}
