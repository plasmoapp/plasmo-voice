val mavenGroup: String by rootProject

group = "$mavenGroup.server-common"

dependencies {
    implementation(project(":api:common"))
    implementation(project(":api:server"))

    implementation(project(":common"))

    implementation(project(":protocol"))

    compileOnly(rootProject.libs.netty)
}
