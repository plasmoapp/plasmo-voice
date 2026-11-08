package su.plo.voice.mac.protocol.message.wire

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.message.Event
import su.plo.voice.mac.protocol.message.Request
import su.plo.voice.mac.protocol.message.Response

private val msgPack = MsgPack(MsgPackConfiguration(ignoreUnknownKeys = true))

fun Request.toFrame(requestId: Int): Frame.Request =
    Frame.Request(requestId, msgPack.encodeToByteArray<Request>(this))

fun Response.toFrame(requestId: Int): Frame.Response =
    Frame.Response(requestId, msgPack.encodeToByteArray<Response>(this))

fun Event.toFrame(): Frame.Event =
    Frame.Event(msgPack.encodeToByteArray<Event>(this))

fun Frame.Request.decode(): Request = msgPack.decodeFromByteArray(payload)

fun Frame.Response.decode(): Response = msgPack.decodeFromByteArray(payload)

fun Frame.Event.decode(): Event = msgPack.decodeFromByteArray(payload)
