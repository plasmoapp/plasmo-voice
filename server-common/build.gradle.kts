val mavenGroup: String by rootProject

group = "$mavenGroup.server-common"

dependencies {
    implementation(project(":api:common"))
    implementation(project(":api:server-common"))

    implementation(project(":common"))

    implementation(project(":protocol"))

    implementation(rootProject.libs.config)

    compileOnly(rootProject.libs.netty)
}
