package su.plo.voice.mac.helper.connection

import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

/**
 * The token proves to Minecraft that the connection is ours.
 */
internal fun readToken(path: String): String? = try {
    SystemFileSystem.source(Path(path)).buffered().use { it.readString() }.trim().ifEmpty { null }
} catch (_: IOException) {
    null
}
