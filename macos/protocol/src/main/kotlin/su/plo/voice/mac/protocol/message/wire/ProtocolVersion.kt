package su.plo.voice.mac.protocol.message.wire

import su.plo.voice.mac.protocol.message.Downstream

/**
 * Sent in [Downstream.Hello] so the mod can tell whether it is talking to a helper it understands.
 */
const val PROTOCOL_VERSION = 1
