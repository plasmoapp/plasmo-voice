package su.plo.voice.mac.helper

import su.plo.voice.mac.helper.capture.AudioQueueMicrophone
import su.plo.voice.mac.helper.connection.SerialWriter
import su.plo.voice.mac.helper.connection.connectToLoopback
import su.plo.voice.mac.helper.connection.readToken
import su.plo.voice.mac.helper.permission.SystemMicrophoneAccess
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameWriter
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlin.system.exitProcess

/**
 * Entry point of the microphone helper.
 */
fun main(args: Array<String>) {
    val port = args.option("--port")?.toIntOrNull() ?: quit("--port is missing.")
    val tokenFile = args.option("--token-file") ?: quit("--token-file is missing.")

    val token = readToken(tokenFile) ?: quit("Cannot read the token from $tokenFile.")
    val socket = connectToLoopback(port) ?: quit("Nothing is listening on port $port.")

    try {
        HelperSession(
            token = token,
            reader = FrameReader((socket as RawSource).buffered()),
            sink = SerialWriter(FrameWriter((socket as RawSink).buffered())),
            access = SystemMicrophoneAccess,
            microphone = AudioQueueMicrophone(),
        ).run()
    } finally {
        socket.close()
    }
}

/** Value that follows [name] in the arguments, or null when the flag is absent. */
private fun Array<String>.option(name: String): String? =
    indexOf(name).takeIf { it >= 0 && it + 1 < size }?.let { get(it + 1) }

/**
 * Without a port and a token there is nobody to report to.
 *
 * The message only reaches a terminal when someone runs the helper by hand, which is exactly the
 * moment they are trying to work out what the arguments should have been.
 */
private fun quit(message: String): Nothing {
    println(message)
    exitProcess(1)
}
