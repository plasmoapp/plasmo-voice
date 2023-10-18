package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.Collection;

/**
 * Manages audio codec implementations.
 */
public interface CodecManager {

    /**
     * Creates a new audio encoder with the specified codec information and configuration parameters.
     *
     * @param codecInfo  The codec information specifying the codec to be used.
     * @param sampleRate The sample rate of the audio data.
     * @param stereo     {@code true} if the audio is in stereo format, {@code false} for mono.
     * @param mtuSize    The maximum transmission unit (MTU) size for network communication.
     * @return An instance of the audio encoder with the specified parameters.
     */
    @NotNull <T extends AudioEncoder> T createEncoder(
            @NotNull CodecInfo codecInfo,
            int sampleRate,
            boolean stereo,
            int mtuSize
    );

    /**
     * Creates a new audio decoder with the specified codec information and configuration parameters.
     *
     * @param codecInfo  The codec information specifying the codec to be used.
     * @param sampleRate The sample rate of the audio data.
     * @param stereo     {@code true} if the audio is in stereo format, {@code false} for mono.
     * @param frameSize  The size of the decoding frame.
     * @return An instance of the audio decoder with the specified parameters.
     */
    @NotNull <T extends AudioDecoder> T createDecoder(
            @NotNull CodecInfo codecInfo,
            int sampleRate,
            boolean stereo,
            int frameSize
    );

    /**
     * Registers a codec supplier to provide codec implementations.
     *
     * @param supplier The codec supplier to register.
     */
    void register(@NotNull CodecSupplier<?, ?> supplier);

    /**
     * Unregisters a codec by its name.
     *
     * @param name The name of the codec to unregister.
     * @return {@code true} if the codec was successfully unregistered, {@code false} if the codec was not found.
     */
    boolean unregister(@NotNull String name);

    /**
     * Unregisters a codec supplier.
     *
     * @param supplier The codec supplier to unregister.
     * @return {@code true} if the codec was successfully unregistered, {@code false} if the codec was not found.
     */
    boolean unregister(@NotNull CodecSupplier<?, ?> supplier);

    /**
     * Retrieves a collection of all registered codec suppliers.
     *
     * @return A collection of registered codec suppliers.
     */
    Collection<CodecSupplier<?, ?>> getCodecs();
}
