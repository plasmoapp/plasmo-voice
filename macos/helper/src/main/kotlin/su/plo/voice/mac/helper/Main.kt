package su.plo.voice.mac.helper

import platform.Foundation.NSRunLoop
import platform.Foundation.run
import su.plo.voice.mac.helper.connection.Report
import su.plo.voice.mac.helper.permission.Permissions
import kotlin.system.exitProcess

/**
 * Entry point of the microphone helper.
 */
fun main(args: Array<String>) {
    val report = Report(args.option("--report"))

    // Tells what TCC already thinks of us
    if (args.contains("--check")) {
        report.write(Permissions.status)
        return
    }

    Permissions.request { status ->
        report.write(status)
        exitProcess(0)
    }

    // The callback lands on some background queue, main just parks here until it exits us
    NSRunLoop.mainRunLoop.run()
}

/** Value that follows [name] in the arguments, or null when the flag is absent. */
private fun Array<String>.option(name: String): String? =
    indexOf(name).takeIf { it >= 0 && it + 1 < size }?.let { get(it + 1) }
