dependencies {
    api(project(":api:common"))

    compileOnly(rootProject.libs.netty)

    api(rootProject.libs.config)
    api(rootProject.libs.opus)

    testImplementation(project(":api:common"))
}
