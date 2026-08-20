package su.plo.voice.mac.protocol.message.wire

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameType
import su.plo.voice.mac.protocol.message.Downstream
import su.plo.voice.mac.protocol.message.Upstream

private val msgPack = MsgPack(MsgPackConfiguration(ignoreUnknownKeys = true))

/**
 * Wraps and unwraps [Upstream] / [Downstream] messages as [FrameType.CONTROL] frames, JSON-encoded.
 */
fun Upstream.toFrame(): Frame = encodeAsControlFrame()

fun Downstream.toFrame(): Frame = encodeAsControlFrame()

fun Frame.toUpstream(): Upstream = decodeControlPayload()

fun Frame.toDownstream(): Downstream = decodeControlPayload()

private inline fun <reified T> T.encodeAsControlFrame(): Frame =
    Frame(FrameType.CONTROL, msgPack.encodeToByteArray(this))

private inline fun <reified T> Frame.decodeControlPayload(): T =
    msgPack.decodeFromByteArray(payload)
