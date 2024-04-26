plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.guava)
    implementation(libs.gson) // not sure why, but there is somewhere old gson
}

repositories {
    mavenCentral()
}
