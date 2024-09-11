package su.plo.voice.api.client.event.audio.source

import su.plo.voice.api.client.audio.device.source.DeviceSource
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.event.EventCancellableBase

/**
 * This event is fired when the [ClientAudioSource] is about to write audio samples to [DeviceSource].
 */
data class AudioSourceWriteEvent(
    val source: ClientAudioSource<*>,
    val samples: ShortArray
) : EventCancellableBase()
