val mavenGroup: String by rootProject

group = "$mavenGroup.server-common"

dependencies {
    api(project(":api:server-common"))
    api(project(":common"))

    api(rootProject.libs.crowdin.lib)

    compileOnly(rootProject.libs.netty)
    compileOnly(rootProject.libs.luckperms)
}
