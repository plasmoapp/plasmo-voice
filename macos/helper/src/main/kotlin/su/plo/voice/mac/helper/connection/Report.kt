package su.plo.voice.mac.helper.connection

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getpid
import su.plo.voice.mac.helper.permission.AuthStatus

/**
 * Writes the outcome to [path], and to stdout when there is one.
 */
internal class Report(private val path: String?) {
    fun write(status: AuthStatus) {
        val text = format(status)

        println(text)
        if (path != null) writeToFile(path, text)
    }

    private fun format(status: AuthStatus) = listOf(
        "Status = $status",
        "PID = ${getpid()}",
        "Bundle = ${NSBundle.mainBundle.bundleIdentifier}",
        "Path = ${NSBundle.mainBundle.bundlePath}",
    ).joinToString("\n")

    @OptIn(ExperimentalForeignApi::class)
    private fun writeToFile(path: String, text: String) {
        val file = fopen(path, "w") ?: return

        fputs(text, file)
        fclose(file)
    }
}
