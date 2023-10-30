group = "$group.server-common"

dependencies {
    api(project(":api:server-common"))
    api(project(":common"))

    compileOnly(libs.netty)
    compileOnly(libs.luckperms)
}
