package su.plo.voice.mac.probe

import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.message.Request
import su.plo.voice.mac.protocol.message.Response
import su.plo.voice.mac.protocol.message.wire.decode
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
    println(await<Response.Permission>(send(Request.Permission(prompt))))
}

private fun HelperConnection.record(seconds: Int, file: File) {
    timeout = 10.seconds
    println("${await<Response.Opened>(send(Request.Open(CAPTURE_FORMAT)))}, recording $seconds s")

    val targetBytes = CAPTURE_FORMAT.sampleRate * CAPTURE_FORMAT.channels * BYTES_PER_SAMPLE * seconds
    val samples = ByteArrayOutputStream(targetBytes)
    var frames = 0

    while (samples.size() < targetBytes) {
        val frame = read() ?: error("The helper went away mid recording.")
        if (frame !is Frame.Audio) continue

        samples.write(frame.payload)
        frames++
    }

    send(Request.Close)
    WavFile.write(file, CAPTURE_FORMAT, samples.toByteArray())
    println("Wrote $frames frames to $file.")
}

private inline fun <reified T : Response> HelperConnection.await(requestId: Int): T {
    while (true) {
        val frame = read() ?: error("The helper went away.")
        if (frame !is Frame.Response || frame.requestId != requestId) continue

        return when (val response = frame.decode()) {
            is T -> response
            is Response.Failure -> error("${response.code}: ${response.message}")
            else -> error("The helper answered with an unexpected $response.")
        }
    }
}
