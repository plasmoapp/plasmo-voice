package su.plo.voice.mac.helper

import platform.posix.getpid
import su.plo.voice.mac.helper.capture.DeviceRegistry
import su.plo.voice.mac.helper.capture.Microphone
import su.plo.voice.mac.helper.connection.FrameSink
import su.plo.voice.mac.helper.exception.MicrophoneException
import su.plo.voice.mac.helper.permission.MicrophoneAccess
import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameType
import su.plo.voice.mac.protocol.message.*
import su.plo.voice.mac.protocol.message.status.*
import su.plo.voice.mac.protocol.message.wire.*

/**
 * One connection to the mod, from hello to close.
 */
internal class HelperSession(
    private val token: String,
    private val reader: FrameReader,
    private val sink: FrameSink,
    private val access: MicrophoneAccess,
    private val microphone: Microphone,
) {
    /**
     * Starts the session and blocks the calling thread until the mod disconnects.
     *
     * Sends an initial [Downstream.Hello] with the [token] and process PID, then continuously
     * dispatches incoming frames from [reader].
     *
     * Guarantees that [microphone] resources are cleaned up when the loop finishes or fails.
     */
    fun run() = try {
        send(Downstream.Hello(token, getpid(), PROTOCOL_VERSION))
        DeviceRegistry.onChange(::sendDevices)
        generateSequence { reader.read() }.forEach(::dispatch)
    } finally {
        microphone.close()
    }

    private fun dispatch(frame: Frame) = when (frame.type) {
        FrameType.CONTROL -> dispatch(frame.toUpstream())
        FrameType.PING -> sink.send(frame)
        FrameType.AUDIO -> Unit
    }

    private fun dispatch(message: Upstream) = when (message) {
        is Upstream.Permission -> permission(message.prompt)
        is Upstream.Open -> open(message.format, message.deviceId)
        Upstream.OpenSettings -> access.settings()
        Upstream.ListDevices -> sendDevices()
        Upstream.Close -> close()
    }

    private fun permission(prompt: Boolean) = if (prompt) {
        access.request { send(Downstream.Permission(it)) }
    } else {
        send(Downstream.Permission(access.status))
    }

    private fun sendDevices() =
        send(Downstream.Devices(DeviceRegistry.devices(), DeviceRegistry.defaultId()))

    private fun open(format: CaptureFormat, deviceId: String?) {
        if (access.status != AuthStatus.AUTHORIZED) {
            return fail(FailureCode.PERMISSION_DENIED, "Microphone access is ${access.status}.")
        }

        try {
            val frameSamples = microphone.open(format, deviceId) { sink.send(Frame(FrameType.AUDIO, it)) }
            send(Downstream.Opened(format, frameSamples))
        } catch (e: MicrophoneException) {
            fail(e.error.code, e.message)
        }
    }

    private fun close() {
        microphone.close()
        send(Downstream.Closed)
    }

    private fun fail(code: FailureCode, message: String) =
        send(Downstream.Failure(code, message))

    private fun send(message: Downstream) =
        sink.send(message.toFrame())
}
