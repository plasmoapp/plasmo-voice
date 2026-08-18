package su.plo.voice.client.audio.device.mac

import su.plo.voice.BaseVoice
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.InputDevice
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent
import su.plo.voice.client.audio.device.BaseAudioDevice
import su.plo.voice.mac.protocol.audio.CaptureFormat
import javax.sound.sampled.AudioFormat

private val LOGGER = BaseVoice.createLogger("CoreAudioInputDevice")
private const val BUFFER_MILLIS = 500

/**
 * Reads from the microphone the helper owns, as if it were any other input device.
 */
internal class CoreAudioInputDevice(
    client: PlasmoVoiceClient,
    name: String,
    format: AudioFormat,
    private val supervisor: HelperSupervisor,
) : BaseAudioDevice(client, name, format), InputDevice {
    private val channels = format.channels
    private val samples = ShortRingBuffer(format.sampleRate.toInt() / 1000 * BUFFER_MILLIS * channels)
    private var session: HelperSession? = null

    @Volatile
    private var started = false

    init {
        open()
    }

    override fun open() {
        if (isOpen()) throw DeviceException("Device is already open.")

        val preOpen = DevicePreOpenEvent(this)
        voiceClient.eventBus.fire(preOpen)
        if (preOpen.isCancelled) throw DeviceException("Device opening has been canceled.")

        val session = supervisor.session()
        session.onAudio = { if (started) samples.write(it) }
        val frameSamples = session.openMicrophone(CaptureFormat(format.sampleRate.toInt(), channels))
        this.session = session

        LOGGER.info("Device {} initialized, {} samples per frame.", name, frameSamples)
        voiceClient.eventBus.fire(DeviceOpenEvent(this))
    }

    override fun close() {
        val session = this.session ?: return
        this.session = null
        started = false

        if (session.alive) runCatching { session.closeMicrophone() }
        samples.clear()

        LOGGER.info("Device {} closed.", name)
        voiceClient.eventBus.fire(DeviceClosedEvent(this))
    }

    override fun isOpen() = session?.alive == true

    override fun start() {
        started = true
    }

    override fun stop() {
        started = false
        samples.clear()
    }

    override fun isStarted() = started

    override fun available() = samples.available / channels

    override fun read(frameSize: Int): ShortArray? = samples.read(frameSize * channels)
}
