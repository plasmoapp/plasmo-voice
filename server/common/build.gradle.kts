val mavenGroup: String by rootProject

group = "$mavenGroup.server"

dependencies {
    api(project(":api:server"))
    api(project(":server-common"))

    compileOnly(rootProject.libs.luckperms)
    compileOnly(rootProject.libs.netty)
}
