val fabricLoaderVersion = "0.14.8"

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
}

architectury {
    common("fabric", "forge")
}

sourceSets {
    main {
        resources {
            srcDirs(
                project(":client:common").sourceSets.main.get().resources,
                project(":server:common").sourceSets.main.get().resources
            )
        }
    }
}
