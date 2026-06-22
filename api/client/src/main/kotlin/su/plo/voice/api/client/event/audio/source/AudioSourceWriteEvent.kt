package su.plo.voice.api.client.event.audio.source

import su.plo.voice.api.client.audio.device.source.DeviceSource
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.event.EventCancellableBase

/**
 * This event is fired when the [ClientAudioSource] is about to write audio samples to [DeviceSource].
 *
 * It is fired on every write regardless of audibility, including while the source's
 * [ClientAudioSource.effectiveVolume] is `0` (e.g. out of range, muted via sliders or fully occluded),
 * because volume is applied to the device source, not to the [samples], so the samples here are always
 * full-amplitude decoded PCM. Check [ClientAudioSource.effectiveVolume] if you only care about audible writes.
 */
data class AudioSourceWriteEvent(
    val source: ClientAudioSource<*>,
    val sequenceNumber: Long,
    val samples: ShortArray
) : EventCancellableBase()
