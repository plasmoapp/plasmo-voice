package su.plo.voice.mac.probe

import java.io.File

/**
 * Stands in for Minecraft and starts the helper the two ways that matter.
 *
 * - `open` lets the helper answer for itself
 * - `exec` makes macOS blame whoever started the JVM
 */
fun main(args: Array<String>) {
    val app = File(args.getOrNull(0) ?: error("Usage: probe <Plasmo Voice Microphone.app> [open|exec] [helper args]"))
    val report = File.createTempFile("pvmic", ".txt").also { it.delete() }
    val helper = listOf("--report", report.absolutePath) + args.drop(2)

    val command = when (args.getOrNull(1) ?: "open") {
        "open" -> listOf("/usr/bin/open", "-n", "-g", "-a", app.absolutePath, "--args") + helper
        "exec" -> listOf(app.resolve("Contents/MacOS/PVMicHelper").absolutePath) + helper
        else -> error("Unknown mode.")
    }

    val start = System.currentTimeMillis()
    ProcessBuilder(command).inheritIO().start()

    while (System.currentTimeMillis() - start < TIMEOUT_MS) {
        if (report.length() > 0L) return println("${report.readText()}, elapsed = ${System.currentTimeMillis() - start} ms")
        Thread.sleep(50L)
    }

    error("Helper never answered.")
}

private const val TIMEOUT_MS = 120_000L
