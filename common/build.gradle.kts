plugins {
    id("su.plo.voice.maven-publish")
    alias(libs.plugins.buildconfig)
}

dependencies {
    api(project(":api:common"))
    api(libs.config)

    compileOnly(libs.netty)

    implementation(libs.opus.jni)
    implementation(libs.opus.concentus)
}

buildConfig {
    packageName(rootProject.group.toString())
    className("BuildConstants")
    useJavaOutput()
    buildConfigField("VERSION", project.version.toString())
    buildConfigField("GITHUB_CROWDIN_URL", "https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/pv.zip")
}
