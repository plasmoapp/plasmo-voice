package su.plo.voice.api.client.audio.filter

import su.plo.voice.api.client.audio.device.AudioDevice

/**
 * Processing context of the [AudioFilter]
 */
class AudioFilterContext(
    val device: AudioDevice
) {

    /**
     * Count of channels in the processing context.
     *
     * Some filters can modify the count of channels, such as a stereo to mono filter.
     *
     * @return The count of channels.
     */
    var channels = device.format.channels
}
