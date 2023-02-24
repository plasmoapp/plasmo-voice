val mavenGroup: String by rootProject

group = "$mavenGroup.api"

dependencies {
    api(project(":api:common"))
}
