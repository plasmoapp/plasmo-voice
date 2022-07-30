dependencies {
    compileOnly(project(":api:common"))

    implementation(rootProject.libs.opus)

    testImplementation(project(":api:common"))
}
