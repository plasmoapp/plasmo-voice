package su.plo.voice.mac.probe

import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Launches the helper app and hands back a connected [HelperConnection].
 */
class HelperHost(private val app: File) {
    @OptIn(ExperimentalUuidApi::class)
    fun connect(): HelperConnection {
        val token = Uuid.random().toString()
        val tokenPath = createTokenFile(token)

        return ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { server ->
            server.soTimeout = 15_000
            launchHelper(server.localPort, tokenPath)

            HelperConnection.handshake(server.accept(), token)
        }
    }

    private fun createTokenFile(token: String): Path =
        createTempFile("pvmic", ".token", PERM_OWNER_ONLY).apply {
            toFile().deleteOnExit()
            writeText(token)
        }

    private fun launchHelper(port: Int, tokenPath: Path) {
        ProcessBuilder(
            "/usr/bin/open", "-n", "-g", "-a", app.absolutePath,
            "--args", "--port", "$port", "--token-file", "$tokenPath"
        ).inheritIO().start()
    }

    private companion object {
        val PERM_OWNER_ONLY = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))!!
    }
}
