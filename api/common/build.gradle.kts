val mavenGroup: String by rootProject

group = "$mavenGroup.api"

dependencies {
    api("su.plo.slib:api-common:${rootProject.libs.versions.crosslib.get()}")
}
