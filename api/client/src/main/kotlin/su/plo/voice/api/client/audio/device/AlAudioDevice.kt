package su.plo.voice.api.client.audio.device

import kotlinx.coroutines.Runnable
import java.util.concurrent.CompletableFuture

// todo: doc
interface AlAudioDevice : AudioDevice {

    /**
     * Gets the device's pointer
     */
    val devicePointer: Long

    /**
     * Gets the device's context pointer
     */
    val contextPointer: Long

    /**
     * Runs runnable in the device's context
     */
    @JvmSynthetic
    suspend fun runInContext(runnable: suspend () -> Unit)

    /**
     * Runs runnable in the device's context
     */
    fun runInContextAsync(runnable: Runnable): CompletableFuture<Void?>

    /**
     * Runs runnable in the device's context
     */
    fun runInContextBlocking(runnable: Runnable) = runInContextAsync(runnable).get()
}
