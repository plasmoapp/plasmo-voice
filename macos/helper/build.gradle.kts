import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
}

val appName = "Plasmo Voice Microphone"
val appExecutable = "PVMicHelper"
val appIdentifier = "com.plasmoverse.plasmovoice.mic"

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

    @get:Input
    abstract val executableName: Property<String>

    @get:Input
    abstract val identifier: Property<String>

    @get:OutputDirectory
    abstract val bundle: DirectoryProperty

    @get:Inject
    abstract val exec: ExecOperations

    @TaskAction
    fun bundle() {
        val app = bundle.get().asFile
        val macOs = app.resolve("Contents/MacOS")

        app.deleteRecursively()
        macOs.mkdirs()

        infoPlist.get().asFile.copyTo(app.resolve("Contents/Info.plist"))

        val executable = macOs.resolve(executableName.get())
        exec.exec {
            commandLine("/usr/bin/lipo", "-create", "-output", executable.absolutePath)
            args(binaries.files.map { it.absolutePath })
        }
        executable.setExecutable(true, false)

        exec.exec {
            commandLine(
                "/usr/bin/codesign", "--force", "--sign", "-",
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
    executableName.set(appExecutable)
    identifier.set(appIdentifier)
    bundle.set(layout.buildDirectory.dir("bundle/$appName.app"))
}

tasks.named("build") {
    dependsOn(bundleApp)
}
