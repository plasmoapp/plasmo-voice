package su.plo.voice.mac.helper.connection

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread

private const val MAX_TOKEN_SIZE = 128

/**
 * The token proves to Minecraft that the connection is ours.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun readToken(path: String): String? {
    /*
     * fopen()
     * https://pubs.opengroup.org/onlinepubs/9699919799/functions/fopen.html
     */
    val file = fopen(path, "r") ?: return null

    try {
        val buffer = ByteArray(MAX_TOKEN_SIZE)

        /*
         * fread()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/fread.html
         */
        val read = buffer.usePinned { fread(it.addressOf(0), 1.convert(), buffer.size.convert(), file) }

        return buffer.decodeToString(0, read.toInt()).trim().ifEmpty { null }
    } finally {
        /*
         * fclose()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/fclose.html
         */
        fclose(file)
    }
}
