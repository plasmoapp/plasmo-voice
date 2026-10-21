package su.plo.voice.client.audio.device.mac

import su.plo.voice.BaseVoice
import su.plo.voice.client.mac.AVAuthorizationStatus
import su.plo.voice.client.mac.AVCaptureDevice
import su.plo.voice.mac.protocol.message.status.AuthStatus
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.concurrent.thread

private val LOGGER = BaseVoice.createLogger("LauncherMicrophone")

private const val MICROPHONE_SETTINGS = "x-apple.systempreferences:com.apple.preference.security?Privacy_Microphone"

internal object LauncherMicrophone {
    private val asking = AtomicBoolean()
    private val resolving = AtomicBoolean()

    @Volatile
    private var promptless = false

    private val status: AVAuthorizationStatus
        get() = AVCaptureDevice.authorizationStatus

    @JvmStatic
    val undecided: Boolean
        get() = !promptless && status == AVAuthorizationStatus.NOT_DETERMINED

    @JvmStatic
    val needsHelper: Boolean
        get() = promptless && status == AVAuthorizationStatus.NOT_DETERMINED

    @JvmStatic
    val refusal: AuthStatus?
        get() = when (status) {
            AVAuthorizationStatus.DENIED -> AuthStatus.DENIED
            AVAuthorizationStatus.RESTRICTED -> AuthStatus.RESTRICTED
            else -> null
        }

    @JvmStatic
    fun request(onAnswer: Runnable): Boolean {
        if (!undecided) return false
        if (!asking.compareAndSet(false, true)) return true

        thread(name = "Plasmo Voice Microphone Permission", isDaemon = true) {
            try {
                probe()
            } catch (e: Exception) {
                LOGGER.warn("Failed to probe the launcher's microphone permission", e)
            }

            val answer = status
            LOGGER.info("Launcher microphone permission after the probe: {}", answer)
            promptless = answer == AVAuthorizationStatus.NOT_DETERMINED
            asking.set(false)
            onAnswer.run()
        }

        return true
    }

    // AVCaptureDevice requestAccess can't be used to prompt,
    // macOS crashes the whole process when the launcher has no NSMicrophoneUsageDescription
    private fun probe() {
        val line = AudioSystem.getTargetDataLine(
            AudioFormat(
                48_000f,
                16,
                1,
                true,
                false,
            )
        )

        line.open()

        line.use { line ->
            line.start()
            line.stop()
        }
    }

    // mirrors CoreAudioInputDeviceFactory#awaitManualGrant
    @JvmStatic
    fun resolve(onGranted: Runnable) {
        if (!resolving.compareAndSet(false, true)) return

        thread(name = "Plasmo Voice Microphone Permission", isDaemon = true) {
            try {
                ProcessBuilder("/usr/bin/open", MICROPHONE_SETTINGS).start()

                val deadline = System.currentTimeMillis() + CoreAudioInputDeviceFactory.MANUAL_GRANT_TIMEOUT_MS
                while (status != AVAuthorizationStatus.AUTHORIZED && System.currentTimeMillis() < deadline) {
                    Thread.sleep(CoreAudioInputDeviceFactory.MANUAL_GRANT_POLL_MS)
                }

                if (status == AVAuthorizationStatus.AUTHORIZED) onGranted.run()
            } catch (e: Exception) {
                LOGGER.warn("Failed to resolve the launcher's microphone permission", e)
            } finally {
                resolving.set(false)
            }
        }
    }
}
