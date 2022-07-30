val fabricLoaderVersion = "0.14.8"

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
}

architectury {
    common("fabric")
}

configurations {
    create("dev")
}

tasks {
    artifacts {
        add("dev", jar)
    }
}
