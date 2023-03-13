package su.plo.voice.api.client.audio.device.source

import su.plo.voice.api.client.audio.device.AudioDevice
import java.util.concurrent.CompletableFuture

// todo: doc
interface DeviceSource {

    val device: AudioDevice

    fun write(samples: ByteArray)

    suspend fun close()

    fun closeAsync(): CompletableFuture<Void?>

    fun isClosed(): Boolean
}

