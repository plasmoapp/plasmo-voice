package su.plo.voice.client.audio.device.mac

import su.plo.voice.api.client.audio.device.DeviceException
import java.io.File

private const val VERSION = "1.0.0"
private const val APP_NAME = "Plasmo Voice Microphone.app"
private const val EXECUTABLE = "Contents/MacOS/PVMicHelper"
private const val RESOURCE = "/natives/macos/helper.zip"
private const val OVERRIDE_PROPERTY = "plasmovoice.mac.helper"

/**
 * Unpacks the helper bundle once per version.
 *
 * It lands outside the game directory. TCC keys its answer to the signature, so one copy
 * shared by every instance means the player is asked for the microphone exactly once.
 */
internal object HelperInstaller {
    private val root = File(System.getProperty("user.home"), "Library/Application Support/PlasmoVoice")

    fun ensureInstalled(): File {
        System.getProperty(OVERRIDE_PROPERTY)?.let { return File(it) }

        val app = File(root, APP_NAME)
        val stamp = File(root, ".helper-version")
        if (isHealthy(app, stamp)) return app

        val archive = File.createTempFile("pvmic", ".zip")
        try {
            val resource = javaClass.getResourceAsStream(RESOURCE)
                ?: throw DeviceException("The macOS microphone helper is not bundled in this build.")
            resource.use { input -> archive.outputStream().use(input::copyTo) }

            app.deleteRecursively()
            root.mkdirs()
            unpack(archive, root)

            if (!File(app, EXECUTABLE).canExecute()) throw DeviceException("The helper archive does not contain a working $APP_NAME.")
            hide(app)
            stamp.writeText(VERSION)
        } finally {
            archive.delete()
        }

        return app
    }

    private fun isHealthy(app: File, stamp: File) =
        app.isDirectory && stamp.isFile && stamp.readText() == VERSION && File(app, EXECUTABLE).canExecute()

    @Suppress("SameParameterValue")
    private fun unpack(archive: File, target: File) {
        // Warning: don't use java.util.zip. The latter drops the code signature and TCC then refuses
        val process = ProcessBuilder("/usr/bin/ditto", "-x", "-k", archive.path, target.path)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.reader().use { it.readText() }
        if (process.waitFor() != 0) throw DeviceException("Failed to unpack the helper: ${output.trim()}.")
    }

    private fun hide(app: File) {
        ProcessBuilder("/usr/bin/chflags", "hidden", app.path).start().waitFor()
    }
}
