package su.plo.voice.client.audio.device.mac

import su.plo.voice.BaseVoice
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.mac.protocol.message.Downstream
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.UUID

private const val HANDSHAKE_TIMEOUT_MS = 15_000
private const val RETRY_DELAY_MS = 2_000L
private const val MAX_RETRY_DELAY_MS = 60_000L

private val TOKEN_OWNER_ONLY = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))

/**
 * Keeps at most one helper process around, and backs off when it will not start.
 */
internal class HelperSupervisor(private val onDevices: (Downstream.Devices) -> Unit) {
    private val logger = BaseVoice.createLogger("HelperSupervisor")
    private var session: HelperSession? = null
    private var failures = 0
    private var retryAt = 0L

    @Synchronized
    fun restart() {
        session?.close()
        session = null
        failures = 0
        retryAt = 0L
    }

    @Synchronized
    fun session(): HelperSession {
        session?.takeIf { it.alive }?.let { return it }

        if (System.currentTimeMillis() < retryAt) {
            throw DeviceException("The macOS microphone helper failed to start $failures time(s), waiting before the next try")
        }

        try {
            return spawn().also {
                session = it
                failures = 0
                retryAt = 0L
                logger.info("macOS microphone helper started, PID: {}", it.pid)
            }
        } catch (e: Exception) {
            failures++
            retryAt = System.currentTimeMillis() + minOf(RETRY_DELAY_MS shl (failures - 1), MAX_RETRY_DELAY_MS)
            throw e as? DeviceException ?: DeviceException("Failed to start the macOS microphone helper", e)
        }
    }

    private fun spawn(): HelperSession {
        val app = HelperInstaller.ensureInstalled()
        val token = UUID.randomUUID().toString()

        // Everything there is readable by every user through ps
        val tokenFile = Files.createTempFile("pvmic", ".token", TOKEN_OWNER_ONLY)
        Files.write(tokenFile, token.toByteArray())

        try {
            return ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { server ->
                server.soTimeout = HANDSHAKE_TIMEOUT_MS

                // Never as our own child, so only then does the helper answer to macOS for itself
                // instead of inheriting the launcher's missing permission.
                ProcessBuilder(
                    "/usr/bin/open", "-n", "-g", "-a", app.absolutePath,
                    "--args", "--port", "${server.localPort}", "--token-file", "$tokenFile"
                ).start()

                val socket = server.accept()
                socket.tcpNoDelay = true

                HelperSession(socket, token).also { it.onDevices = onDevices }
            }
        } finally {
            Files.deleteIfExists(tokenFile)
        }
    }
}
