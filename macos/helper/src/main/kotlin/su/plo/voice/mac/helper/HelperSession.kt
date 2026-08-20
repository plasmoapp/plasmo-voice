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
        /*
         * getpid()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/getpid.html
         */
        send(Downstream.Hello(token, getpid(), PROTOCOL_VERSION))
        DeviceRegistry.onChange { sendDevices() }
        generateSequence { reader.read() }.forEach(::dispatch)
    } finally {
        DeviceRegistry.stopListening()
        microphone.close()
    }

    private fun dispatch(frame: Frame) = when (frame.type) {
        FrameType.CONTROL -> dispatch(frame.toUpstream())
        FrameType.PING -> sink.send(frame)
        FrameType.AUDIO -> Unit
    }

    private fun dispatch(message: Upstream) = when (message) {
        is Upstream.Permission -> permission(message.prompt, message.requestId)
        is Upstream.Open -> open(message.format, message.deviceId, message.requestId)
        Upstream.OpenSettings -> access.settings()
        is Upstream.ListDevices -> sendDevices(message.requestId)
        is Upstream.Close -> close(message.requestId)
    }

    private fun permission(prompt: Boolean, requestId: Int) = if (prompt) {
        access.request { send(Downstream.Permission(it, requestId)) }
    } else {
        send(Downstream.Permission(access.status, requestId))
    }

    private fun sendDevices(requestId: Int? = null) =
        send(Downstream.Devices(DeviceRegistry.devices(), DeviceRegistry.defaultId(), requestId))

    private fun open(format: CaptureFormat, deviceId: String?, requestId: Int) {
        if (access.status != AuthStatus.AUTHORIZED) {
            return fail(FailureCode.PERMISSION_DENIED, "Microphone access is ${access.status}.", requestId)
        }

        try {
            val frameSamples = microphone.open(format, deviceId) { sink.send(Frame(FrameType.AUDIO, it)) }
            send(Downstream.Opened(format, frameSamples, requestId))
        } catch (e: MicrophoneException) {
            fail(e.error.code, e.message, requestId)
        }
    }

    private fun close(requestId: Int) {
        microphone.close()
        send(Downstream.Closed(requestId))
    }

    private fun fail(code: FailureCode, message: String, requestId: Int?) =
        send(Downstream.Failure(code, message, requestId))

    private fun send(message: Downstream) =
        sink.send(message.toFrame())
}
