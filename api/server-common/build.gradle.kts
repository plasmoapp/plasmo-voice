val mavenGroup: String by rootProject

group = "$mavenGroup.api"

dependencies {
    implementation(project(":api:common"))

    implementation(project(":protocol"))
}
