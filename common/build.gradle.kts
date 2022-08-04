dependencies {
    compileOnly(project(":api:common"))

    implementation(project(":protocol"))

    implementation(rootProject.libs.netty)
    implementation(rootProject.libs.nightconfig)
    implementation(rootProject.libs.opus)

    testImplementation(project(":api:common"))
}
