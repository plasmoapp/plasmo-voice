val mavenGroup: String by rootProject

group = "$mavenGroup.proxy-common"

dependencies {
    implementation(project(":api:common"))
    implementation(project(":api:server-common"))
    implementation(project(":api:proxy"))
//
//    implementation(project(":server:common"))
    implementation(project(":common"))
    implementation(project(":server-common"))

    implementation(rootProject.libs.config)
    compileOnly(rootProject.libs.netty)

    implementation(project(":protocol"))
}
