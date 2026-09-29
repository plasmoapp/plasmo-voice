plugins {
    id("su.plo.voice.maven-publish")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.grgit)
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
    buildConfigField("GIT_HASH", grgit.head().abbreviatedId.substring(0, 7))
    buildConfigField("GITHUB_CROWDIN_URL", "https://github.com/plasmoapp/plasmo-voice-crowdin/archive/refs/heads/pv.zip")
}
