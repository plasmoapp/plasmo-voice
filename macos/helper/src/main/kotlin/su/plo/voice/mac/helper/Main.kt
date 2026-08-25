package su.plo.voice.mac.helper

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import su.plo.voice.mac.helper.capture.AudioQueueMicrophone
import su.plo.voice.mac.helper.connection.SerialWriter
import su.plo.voice.mac.helper.connection.connectToLoopback
import su.plo.voice.mac.helper.connection.readToken
import su.plo.voice.mac.helper.permission.SystemMicrophoneAccess
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameWriter
import kotlin.system.exitProcess

/**
 * Entry point of the microphone helper.
 */
fun main(args: Array<String>) {
    val port = args.option("--port")?.toIntOrNull() ?: quit("--port is missing.")
    val tokenFile = args.option("--token-file") ?: quit("--token-file is missing.")

    val token = readToken(tokenFile) ?: quit("Cannot read the token from $tokenFile.")
    val socket = connectToLoopback(port) ?: quit("Nothing is listening on port $port.")

    // Accessory keeps the helper out of the Dock and the switcher, while still leaving it an app
    // macOS is willing to put a permission dialog in front of.

    /*
     * NSApplication.sharedApplication()
     * https://developer.apple.com/documentation/appkit/nsapplication/shared
     */
    val application = NSApplication.sharedApplication()
    /*
     * setActivationPolicy()
     * https://developer.apple.com/documentation/appkit/nsapplication/setactivationpolicy(_:)
     */
    application.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyAccessory)

    val writer = SerialWriter(FrameWriter((socket as RawSink).buffered()), onLost = socket::shutdown)
    val session = HelperSession(
        token = token,
        reader = FrameReader((socket as RawSource).buffered()),
        sink = writer,
        access = SystemMicrophoneAccess,
        microphone = AudioQueueMicrophone(),
    )

    /*
     * dispatch_async()
     * https://developer.apple.com/documentation/dispatch/dispatch_async
     */
    dispatch_async(
        /*
         * dispatch_queue_create()
         * https://developer.apple.com/documentation/dispatch/dispatch_queue_create
         */
        dispatch_queue_create("com.plasmovoice.mic.session", null)
    ) {
        try {
            session.run()
        } finally {
            socket.close()
            exitProcess(0)
        }
    }

    /*
     * run()
     * https://developer.apple.com/documentation/appkit/nsapplication/run()
     */
    application.run()
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
