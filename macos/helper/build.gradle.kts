import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
}

val appName = "Plasmo Voice Microphone"
val appExecutable = "PVMicHelper"
val appIdentifier = "com.plasmoverse.plasmovoice.mic"
val appArchive = "$appExecutable.zip"

kotlin {
    listOf(macosX64(), macosArm64()).forEach { target: KotlinNativeTarget ->
        target.binaries.executable {
            baseName = appExecutable
            entryPoint = "su.plo.voice.mac.helper.main"
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets["macosMain"].apply {
        kotlin.setSrcDirs(listOf("src/main/kotlin"))
        dependencies {
            implementation(project(":macos:protocol"))
        }
    }
}

abstract class BundleMacApp : DefaultTask() {
    @get:InputFiles
    abstract val binaries: ConfigurableFileCollection

    @get:InputFile
    abstract val infoPlist: RegularFileProperty

    @get:InputFile
    abstract val iconFile: RegularFileProperty

    @get:Input
    abstract val executableName: Property<String>

    @get:Input
    abstract val identifier: Property<String>

    @get:Input
    @get:Optional
    abstract val signingIdentity: Property<String>

    @get:OutputDirectory
    abstract val bundle: DirectoryProperty

    @get:Inject
    abstract val exec: ExecOperations

    @TaskAction
    fun bundle() {
        val app = bundle.get().asFile
        val macOs = app.resolve("Contents/MacOS")
        val resources = app.resolve("Contents/Resources")

        app.deleteRecursively()
        macOs.mkdirs()
        resources.mkdirs()

        infoPlist.get().asFile.copyTo(app.resolve("Contents/Info.plist"))
        iconFile.get().asFile.copyTo(resources.resolve("AppIcon.icns"))

        val executable = macOs.resolve(executableName.get())
        exec.exec {
            commandLine("/usr/bin/lipo", "-create", "-output", executable.absolutePath)
            args(binaries.files.map { it.absolutePath })
        }
        executable.setExecutable(true, false)

        val identity = signingIdentity.orNull?.takeIf { it.isNotBlank() } ?: "-"
        if (identity == "-") {
            logger.lifecycle("Signing ${app.name} ad-hoc, the microphone grant will not survive an update.")
        }

        exec.exec {
            commandLine(
                "/usr/bin/codesign", "--force", "--sign", identity,
                "--identifier", identifier.get(),
                app.absolutePath,
            )
        }
    }
}

val releaseBinaries = kotlin.targets.withType(KotlinNativeTarget::class.java)
    .flatMap { it.binaries.filterIsInstance<Executable>() }
    .filter { it.buildType == NativeBuildType.RELEASE }

val bundleApp = tasks.register<BundleMacApp>("bundleApp") {
    description = "Bundles macOS app."
    onlyIf { System.getProperty("os.name").startsWith("Mac") }

    dependsOn(releaseBinaries.map { it.linkTaskName })

    binaries.from(releaseBinaries.map { it.outputFile })
    infoPlist.set(layout.projectDirectory.file("src/bundle/Info.plist"))
    iconFile.set(layout.projectDirectory.file("src/bundle/AppIcon.icns"))
    executableName.set(appExecutable)
    identifier.set(appIdentifier)
    signingIdentity.set(providers.gradleProperty("pv.mac.signIdentity"))
    bundle.set(layout.buildDirectory.dir("bundle/$appName.app"))
}

val bundleZip = tasks.register<Exec>("bundleZip") {
    description = "Packs the macOS app into a zip the mod can ship."
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }

    dependsOn(bundleApp)

    val app = bundleApp.flatMap { it.bundle }
    val archive = layout.buildDirectory.file("distributions/$appArchive")

    inputs.dir(app)
    outputs.file(archive)

    commandLine("/usr/bin/ditto", "-c", "-k", "--keepParent")
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(app.get().asFile.absolutePath, archive.get().asFile.absolutePath)
    })

    doFirst { archive.get().asFile.parentFile.mkdirs() }
}

val macHelperBundle: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts.add(macHelperBundle.name, bundleZip.map { it.outputs.files.singleFile }) {
    builtBy(bundleZip)
    type = "zip"
    extension = "zip"
}

tasks.named("build") {
    dependsOn(bundleApp)
}
