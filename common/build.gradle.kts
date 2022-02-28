val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricVersion: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

    // YAML for server config
    compileOnly("org.yaml:snakeyaml:1.29")

    // Plasmo Voice protocol
    compileOnly("su.plo.voice:common:1.0.0")

    // Opus
    compileOnly("su.plo.voice:opus:1.1.2")

    // RNNoise
    compileOnly("su.plo.voice:rnnoise:1.0.0")
}

architectury {
    common(false)
}

configurations {
    create("dev")
}

tasks {
    artifacts {
        add("dev", jar)
    }
}
