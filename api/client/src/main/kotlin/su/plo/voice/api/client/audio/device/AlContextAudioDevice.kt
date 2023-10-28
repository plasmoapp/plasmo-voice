package su.plo.voice.api.client.audio.device

import kotlinx.coroutines.Runnable

import java.util.concurrent.CompletableFuture

/**
 * Represents an OpenAL audio device with context.
 */
interface AlContextAudioDevice : AudioDevice {

    /**
     * Gets the pointer to the OpenAL audio device.
     *
     * @return The device pointer.
     */
    val devicePointer: Long

    /**
     * Gets the pointer to the OpenAL context associated with this device.
     *
     * @return The context pointer.
     */
    val contextPointer: Long

    /**
     * Runs a runnable in the context of the OpenAL audio device.
     *
     * @param runnable The runnable to run in the device's context.
     */
    @JvmSynthetic
    suspend fun runInContext(runnable: suspend () -> Unit)

    /**
     * Runs the runnable in the context of the OpenAL audio device.
     *
     * @param runnable The runnable to run in the device's context.
     * @return A [CompletableFuture] that completes when the operation is done.
     */
    fun runInContextAsync(runnable: Runnable): CompletableFuture<Void?>

    /**
     * Runs the runnable in the context of the OpenAL audio device, blocking the current thread until it completes.
     *
     * @param runnable The runnable to run in the device's context.
     */
    fun runInContextBlocking(runnable: Runnable) = runInContextAsync(runnable).get()
}
