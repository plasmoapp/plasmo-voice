package su.plo.voice.api.client.audio.device

import su.plo.voice.api.client.audio.device.source.AlSource

/**
 * Represents an OpenAL audio output device with context.
 */
interface AlContextOutputDevice : OutputDevice<AlSource>, AlContextAudioDevice, HrtfAudioDevice
