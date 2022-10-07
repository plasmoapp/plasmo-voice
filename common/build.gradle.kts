dependencies {
    compileOnly(project(":api:common"))

    implementation(project(":protocol"))

    compileOnly(rootProject.libs.netty)
    implementation(rootProject.libs.config)
    implementation(rootProject.libs.opus)

    testImplementation(project(":api:common"))
}
