package su.plo.voice.mac.protocol.message

import kotlinx.serialization.json.Json
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameType
import su.plo.voice.mac.protocol.frame.ProtocolException

const val PROTOCOL_VERSION = 1
private val json = Json { ignoreUnknownKeys = true }

/**
 * Control messages travel as JSON.
 */
fun Upstream.toFrame() = Frame(FrameType.CONTROL, json.encodeToString(this).encodeToByteArray())

fun Downstream.toFrame() = Frame(FrameType.CONTROL, json.encodeToString(this).encodeToByteArray())

fun Frame.toUpstream(): Upstream = json.decodeFromString(text())

fun Frame.toDownstream(): Downstream = json.decodeFromString(text())

private fun Frame.text(): String {
    if (type != FrameType.CONTROL) throw ProtocolException("Expected a control frame, got $type.")

    return payload.decodeToString()
}
