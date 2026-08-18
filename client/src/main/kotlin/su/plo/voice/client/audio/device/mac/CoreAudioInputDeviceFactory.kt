package su.plo.voice.client.audio.device.mac

import com.google.common.collect.ImmutableList
import su.plo.voice.BaseVoice
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AudioDevice
import su.plo.voice.api.client.audio.device.DeviceFactory
import su.plo.voice.mac.protocol.audio.DeviceInfo
import su.plo.voice.mac.protocol.message.AuthStatus
import su.plo.voice.mac.protocol.message.Downstream
import javax.sound.sampled.AudioFormat

const val COREAUDIO_INPUT = "COREAUDIO_INPUT"
private val LOGGER = BaseVoice.createLogger("CoreAudioInputDeviceFactory")
private const val MANUAL_GRANT_POLL_MS = 1_500L
private const val MANUAL_GRANT_TIMEOUT_MS = 5 * 60_000L

class CoreAudioInputDeviceFactory(private val client: PlasmoVoiceClient) : DeviceFactory {
    private val supervisor = HelperSupervisor(::cache)

    @Volatile
    private var devices: List<DeviceInfo> = emptyList()

    @Volatile
    private var defaultId: String? = null

    @Volatile
    var lastError: Throwable? = null
        private set

    override fun openDevice(format: AudioFormat, deviceName: String?): AudioDevice = try {
        val device = CoreAudioInputDevice(client, deviceName ?: defaultDeviceName(), format, supervisor, idOf(deviceName))
        lastError = null
        device
    } catch (e: Exception) {
        lastError = e
        throw e
    }

    override fun getDefaultDeviceName() = defaultDeviceName()

    override fun getDeviceNames(): ImmutableList<String> = ImmutableList.copyOf(refresh().map { it.name })

    override fun getName() = COREAUDIO_INPUT

    fun permission(prompt: Boolean): AuthStatus = supervisor.session().permission(prompt)

    fun openSettings() = supervisor.session().openSettings()

    fun resolvePermission(): AuthStatus {
        val status = permission(prompt = false)
        val resolved = if (status == AuthStatus.NOT_DETERMINED) permission(prompt = true) else status
        if (resolved == AuthStatus.AUTHORIZED) return resolved

        openSettings()
        return awaitManualGrant()
    }

    private fun awaitManualGrant(): AuthStatus {
        val deadline = System.currentTimeMillis() + MANUAL_GRANT_TIMEOUT_MS
        var status = permission(prompt = false)

        while (status != AuthStatus.AUTHORIZED && System.currentTimeMillis() < deadline) {
            Thread.sleep(MANUAL_GRANT_POLL_MS)
            status = permission(prompt = false)
        }

        return status
    }

    private fun defaultDeviceName(): String {
        val devices = refresh()
        return devices.firstOrNull { it.id == defaultId }?.name
            ?: devices.firstOrNull()?.name
            ?: "Default"
    }

    private fun idOf(deviceName: String?): String? =
        deviceName?.let { name -> refresh().firstOrNull { it.name == name }?.id }

    private fun refresh(): List<DeviceInfo> {
        devices.takeIf { it.isNotEmpty() }?.let { return it }

        return try {
            cache(supervisor.session().listDevices())
            devices
        } catch (e: Exception) {
            LOGGER.warn("Failed to list macOS input devices.", e)
            emptyList()
        }
    }

    private fun cache(message: Downstream.Devices) {
        devices = message.devices
        defaultId = message.defaultId
    }
}
