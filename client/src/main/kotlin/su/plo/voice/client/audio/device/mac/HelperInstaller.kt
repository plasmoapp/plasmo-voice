package su.plo.voice.client.audio.device.mac

import su.plo.voice.api.client.audio.device.DeviceException
import java.io.File
import java.io.RandomAccessFile
import java.util.zip.CRC32

private const val APP_NAME = "Plasmo Voice Microphone.app"
private const val EXECUTABLE = "Contents/MacOS/PVMicHelper"
private const val RESOURCE = "/natives/macos/helper.zip"
private const val OVERRIDE_PROPERTY = "plasmovoice.mac.helper"

/**
 * Unpacks the helper bundle once per build, keyed by the content hash of the bundled archive.
 *
 * It lands outside the game directory. TCC keys its answer to the signature, so one copy
 * shared by every instance means the player is asked for the microphone exactly once.
 */
internal object HelperInstaller {
    private val root = File(System.getProperty("user.home"), "Library/Application Support/PlasmoVoice")

    @Synchronized
    fun ensureInstalled(): File {
        System.getProperty(OVERRIDE_PROPERTY)?.let { return File(it) }

        val app = File(root, APP_NAME)
        val stamp = File(root, ".helper-version")

        val bytes = javaClass.getResourceAsStream(RESOURCE)?.use { it.readBytes() }
            ?: throw DeviceException("The macOS microphone helper is not bundled in this build. It should never happen")
        val hash = crc32(bytes)

        if (isHealthy(app, stamp, hash)) return app

        root.mkdirs()
        RandomAccessFile(File(root, ".helper-install.lock"), "rw").use { lockFile ->
            val lock = lockFile.channel.lock()
            try {
                if (!isHealthy(app, stamp, hash)) install(app, stamp, bytes, hash)
            } finally {
                lock.release()
            }
        }

        return app
    }

    private fun install(app: File, stamp: File, bytes: ByteArray, hash: String) {
        val archive = File.createTempFile("pvmic", ".zip")
        try {
            archive.writeBytes(bytes)

            app.deleteRecursively()
            unpack(archive, root)

            if (!File(app, EXECUTABLE).canExecute()) throw DeviceException("The helper archive does not contain a working $APP_NAME")
            hide(app)
            stamp.writeText(hash)
        } finally {
            archive.delete()
        }
    }

    private fun isHealthy(app: File, stamp: File, hash: String) =
        app.isDirectory && stamp.isFile && stamp.readText() == hash && File(app, EXECUTABLE).canExecute()

    private fun crc32(bytes: ByteArray): String =
        CRC32().apply { update(bytes) }.value.toString(16)

    @Suppress("SameParameterValue")
    private fun unpack(archive: File, target: File) {
        // Warning: don't use java.util.zip. The latter drops the code signature and TCC then refuses
        val process = ProcessBuilder("/usr/bin/ditto", "-x", "-k", archive.path, target.path)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.reader().use { it.readText() }
        if (process.waitFor() != 0) throw DeviceException("Failed to unpack the helper: ${output.trim()}")
    }

    private fun hide(app: File) {
        ProcessBuilder("/usr/bin/chflags", "hidden", app.path).start().waitFor()
    }
}
