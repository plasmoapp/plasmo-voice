val mavenGroup: String by rootProject

group = "$mavenGroup.api"

dependencies {
    implementation(project(":protocol"))
    shadow(project(":protocol"))
}
