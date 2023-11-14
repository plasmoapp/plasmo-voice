group = "$group.server-common"

dependencies {
    api(project(":api:server-common"))
    api(project(":common"))

    api(libs.crowdin)

    compileOnly(libs.netty)
    compileOnly(libs.luckperms)
}
