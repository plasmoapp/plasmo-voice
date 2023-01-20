val mavenGroup: String by rootProject

group = "$mavenGroup.server"

dependencies {
    implementation(project(":api:common"))
    implementation(project(":api:server"))
    implementation(project(":api:server-common"))

    implementation(project(":common"))
    implementation(project(":server-common"))

    implementation(project(":protocol"))

    implementation(rootProject.libs.config)
    compileOnly(rootProject.libs.luckperms)

    compileOnly(rootProject.libs.netty)
}
