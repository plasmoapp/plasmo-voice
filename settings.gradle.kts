pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }

        gradlePluginPortal()
    }
}

rootProject.name = "PlasmoVoice"