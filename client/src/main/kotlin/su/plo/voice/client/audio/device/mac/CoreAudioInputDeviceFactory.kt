package su.plo.voice.client.audio.device.mac

import com.google.common.collect.ImmutableList
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AudioDevice
import su.plo.voice.api.client.audio.device.DeviceFactory
import su.plo.voice.mac.protocol.message.AuthStatus
import javax.sound.sampled.AudioFormat

const val COREAUDIO_INPUT = "COREAUDIO_INPUT"
private const val DEFAULT_DEVICE = "Default"

class CoreAudioInputDeviceFactory(private val client: PlasmoVoiceClient) : DeviceFactory {
    private val supervisor = HelperSupervisor()

    override fun openDevice(format: AudioFormat, deviceName: String?): AudioDevice =
        CoreAudioInputDevice(client, deviceName ?: DEFAULT_DEVICE, format, supervisor)

    override fun getDefaultDeviceName() = DEFAULT_DEVICE

    override fun getDeviceNames(): ImmutableList<String> = ImmutableList.of(DEFAULT_DEVICE)

    override fun getName() = COREAUDIO_INPUT

    fun permission(prompt: Boolean): AuthStatus = supervisor.session().permission(prompt)

    fun openSettings() = supervisor.session().openSettings()
}
