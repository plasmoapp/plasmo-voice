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
import su.plo.voice.mac.protocol.message.Devices
import su.plo.voice.mac.protocol.message.Event
import su.plo.voice.mac.protocol.message.Request
import su.plo.voice.mac.protocol.message.Response
import su.plo.voice.mac.protocol.message.status.AuthStatus
import su.plo.voice.mac.protocol.message.status.FailureCode
import su.plo.voice.mac.protocol.message.wire.PROTOCOL_VERSION
import su.plo.voice.mac.protocol.message.wire.decode
import su.plo.voice.mac.protocol.message.wire.toFrame

internal class HelperSession(
    private val token: String,
    private val reader: FrameReader,
    private val sink: FrameSink,
    private val access: MicrophoneAccess,
    private val microphone: Microphone,
) {
    /**
     * Blocks the calling thread until the mod disconnects.
     */
    fun run() = try {
        /*
         * getpid()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/getpid.html
         */
        emit(Event.Hello(token, getpid(), PROTOCOL_VERSION))
        DeviceRegistry.onChange { emit(devices()) }
        generateSequence { reader.read() }.forEach(::dispatch)
    } finally {
        DeviceRegistry.stopListening()
        microphone.close()
    }

    private fun dispatch(frame: Frame) = when (frame) {
        is Frame.Request -> dispatch(frame.decode()) { sink.send(it.toFrame(frame.requestId)) }
        Frame.Ping -> sink.send(frame)
        is Frame.Response, is Frame.Event, is Frame.Audio -> Unit
    }

    private fun dispatch(request: Request, respond: (Response) -> Unit) = when (request) {
        is Request.Permission -> permission(request.prompt, respond)
        is Request.Open -> respond(open(request.format, request.deviceId))
        Request.OpenSettings -> respond(openSettings())
        Request.ListDevices -> respond(devices())
        Request.Close -> respond(close())
    }

    private fun permission(prompt: Boolean, respond: (Response) -> Unit) = if (prompt) {
        access.request { respond(Response.Permission(it)) }
    } else {
        respond(Response.Permission(access.status))
    }

    private fun open(format: CaptureFormat, deviceId: String?): Response {
        if (access.status != AuthStatus.AUTHORIZED) {
            return Response.Failure(FailureCode.PERMISSION_DENIED, "Microphone access is ${access.status}.")
        }

        return try {
            val frameSamples = microphone.open(format, deviceId) { sink.send(Frame.Audio(it)) }
            Response.Opened(format, frameSamples)
        } catch (e: MicrophoneException) {
            Response.Failure(e.error.code, e.message)
        }
    }

    private fun openSettings(): Response {
        access.settings()
        return Response.SettingsOpened
    }

    private fun close(): Response {
        microphone.close()
        return Response.Closed
    }

    private fun devices() = Devices(DeviceRegistry.devices(), DeviceRegistry.defaultId())

    private fun emit(event: Event) =
        sink.send(event.toFrame())
}
