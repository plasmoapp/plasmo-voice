val mavenGroup: String by rootProject

group = "$mavenGroup.proxy-common"

dependencies {
    api(project(":api:proxy"))
    api(project(":server-common"))

    compileOnly(rootProject.libs.netty)
}
