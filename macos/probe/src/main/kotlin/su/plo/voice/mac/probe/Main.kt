package su.plo.voice.mac.probe

import su.plo.voice.mac.protocol.audio.*
import su.plo.voice.mac.protocol.frame.*
import su.plo.voice.mac.protocol.message.*
import su.plo.voice.mac.protocol.message.wire.*
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.time.Duration.Companion.seconds

private val CAPTURE_FORMAT = CaptureFormat(sampleRate = 48_000, channels = 1)
private const val BYTES_PER_SAMPLE = 2

private const val USAGE = """
Usage:
  probe <Plasmo Voice Microphone.app> permission [prompt]
  probe <Plasmo Voice Microphone.app> record <seconds> <out.wav>
"""

/**
 * CLI for exercising the macOS microphone helper without launching Minecraft.
 */
fun main(args: Array<String>) {
    val appPath = args.getOrNull(0) ?: error(USAGE)
    val action = args.getOrNull(1)

    HelperHost(File(appPath)).connect().use { helper ->
        println("Helper connected. PID: ${helper.pid}.")

        when (action) {
            "permission" -> helper.permission(prompt = args.getOrNull(2) == "prompt")
            "record" -> {
                val seconds = args.getOrNull(2)?.toIntOrNull() ?: error(USAGE)
                val file = File(args.getOrNull(3) ?: error(USAGE))
                helper.record(seconds, file)
            }
            else -> error(USAGE)
        }
    }
}

private fun HelperConnection.permission(prompt: Boolean) {
    timeout = (if (prompt) 120 else 10).seconds
    send(Upstream.Permission(prompt))

    println(await<Downstream.Permission>())
}

private fun HelperConnection.record(seconds: Int, file: File) {
    timeout = 10.seconds
    send(Upstream.Open(CAPTURE_FORMAT))
    println("${await<Downstream.Opened>()}, recording $seconds s")

    val targetBytes = CAPTURE_FORMAT.sampleRate * CAPTURE_FORMAT.channels * BYTES_PER_SAMPLE * seconds
    val samples = ByteArrayOutputStream(targetBytes)
    var frames = 0

    while (samples.size() < targetBytes) {
        val frame = read() ?: error("The helper went away mid recording.")
        if (frame.type != FrameType.AUDIO) continue

        samples.write(frame.payload)
        frames++
    }

    send(Upstream.Close())
    WavFile.write(file, CAPTURE_FORMAT, samples.toByteArray())
    println("Wrote $frames frames to $file.")
}

private inline fun <reified T : Downstream> HelperConnection.await(): T {
    while (true) {
        val frame = read() ?: error("The helper went away.")
        if (frame.type != FrameType.CONTROL) continue

        when (val message = frame.toDownstream()) {
            is T -> return message
            is Downstream.Failure -> error("${message.code}: ${message.message}")
            else -> Unit
        }
    }
}
