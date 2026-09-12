package su.plo.voice.client.audio.device.mac

import com.google.common.collect.ImmutableList
import su.plo.voice.BaseVoice
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AudioDevice
import su.plo.voice.api.client.audio.device.DeviceFactory
import su.plo.voice.api.client.audio.device.InputDevice
import su.plo.voice.mac.protocol.audio.DeviceInfo
import su.plo.voice.mac.protocol.message.Downstream
import su.plo.voice.mac.protocol.message.status.AuthStatus
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import kotlin.concurrent.thread

const val COREAUDIO_INPUT = "COREAUDIO_INPUT"
private val LOGGER = BaseVoice.createLogger("CoreAudioInputDeviceFactory")

private const val DEVICES_TTL_MS = 10_000L
private const val MANUAL_GRANT_POLL_MS = 2_000L
private const val MANUAL_GRANT_TIMEOUT_MS = 2 * 60_000L

class CoreAudioInputDeviceFactory(private val client: PlasmoVoiceClient) : DeviceFactory {
    private val supervisor = HelperSupervisor(::cache)
    private val listing = AtomicBoolean()
    private val resolving = AtomicBoolean()

    @Volatile
    private var devices: List<DeviceInfo> = emptyList()

    @Volatile
    private var defaultId: String? = null

    @Volatile
    private var listedAt = 0L

    @Volatile
    var lastError: Throwable? = null
        private set

    override fun openDevice(format: AudioFormat, deviceName: String?): AudioDevice = try {
        val device = CoreAudioInputDevice(client, deviceName ?: defaultDeviceName(), format, supervisor, idOf(deviceName))
        lastError = null
        device
    } catch (e: Exception) {
        lastError = e
        if ((e as? MicrophonePermissionException)?.status == AuthStatus.NOT_DETERMINED) prompt()
        throw e
    }

    override fun getDefaultDeviceName() = defaultDeviceName()

    override fun getDeviceNames(): ImmutableList<String> = ImmutableList.copyOf(knownDevices().map { it.name })

    override fun getName() = COREAUDIO_INPUT

    fun openSettings() = supervisor.session().openSettings()

    fun resolvePermission(): AuthStatus = withoutOverlap {
        val status = permission(prompt = false)
        val resolved = if (status == AuthStatus.NOT_DETERMINED) permission(prompt = true) else status
        if (resolved == AuthStatus.AUTHORIZED) return@withoutOverlap resolved

        openSettings()
        awaitManualGrant()
    } ?: AuthStatus.UNKNOWN

    private fun permission(prompt: Boolean): AuthStatus = supervisor.session().permission(prompt)

    private fun prompt() {
        if (resolving.get()) return

        thread(name = "Plasmo Voice Microphone Permission", isDaemon = true) {
            withoutOverlap {
                if (permission(prompt = true) == AuthStatus.AUTHORIZED) reopenInputDevice()
            }
        }
    }

    private fun awaitManualGrant(): AuthStatus {
        val deadline = System.currentTimeMillis() + MANUAL_GRANT_TIMEOUT_MS
        var status = restartAndCheck()

        while (status != AuthStatus.AUTHORIZED && System.currentTimeMillis() < deadline) {
            Thread.sleep(MANUAL_GRANT_POLL_MS)
            status = restartAndCheck()
        }

        return status
    }

    private fun restartAndCheck(): AuthStatus = try {
        supervisor.restart()
        permission(prompt = false)
    } catch (e: Exception) {
        LOGGER.warn("Failed to restart the macOS microphone helper while waiting for permission", e)
        AuthStatus.UNKNOWN
    }

    private fun reopenInputDevice() = try {
        val devices = client.deviceManager
        devices.inputDevice.ifPresent(InputDevice::close)
        devices.setInputDevice(devices.openInputDevice(null))
    } catch (e: Exception) {
        LOGGER.warn("Failed to reopen the input device after the microphone was granted", e)
    }

    private fun defaultDeviceName(): String {
        val devices = knownDevices()
        return devices.firstOrNull { it.id == defaultId }?.name
            ?: devices.firstOrNull()?.name
            ?: "Default"
    }

    private fun idOf(deviceName: String?): String? =
        deviceName?.let { name -> knownDevices().firstOrNull { it.name == name }?.id }

    private fun knownDevices(): List<DeviceInfo> {
        if (System.currentTimeMillis() - listedAt > DEVICES_TTL_MS) listInBackground()

        return devices
    }

    private fun listInBackground() {
        if (!listing.compareAndSet(false, true)) return

        thread(name = "Plasmo Voice Microphone Devices", isDaemon = true) {
            try {
                cache(supervisor.session().listDevices())
            } catch (e: Exception) {
                LOGGER.warn("Failed to list macOS input devices", e)
                listedAt = System.currentTimeMillis()
            } finally {
                listing.set(false)
            }
        }
    }

    private fun cache(message: Downstream.Devices) {
        devices = message.devices
        defaultId = message.defaultId
        listedAt = System.currentTimeMillis()
    }

    private fun <T> withoutOverlap(block: () -> T): T? {
        if (!resolving.compareAndSet(false, true)) return null

        return try {
            block()
        } catch (e: Exception) {
            LOGGER.warn("Failed to resolve the macOS microphone permission", e)
            null
        } finally {
            resolving.set(false)
        }
    }
}
