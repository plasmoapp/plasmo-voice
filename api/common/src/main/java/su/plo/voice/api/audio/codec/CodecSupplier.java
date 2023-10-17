package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

/**
 * Represents a codec supplier for creating audio encoders and decoders of specific codec types.
 *
 * @param <Encoder> The type of the audio encoder to be created.
 * @param <Decoder> The type of the audio decoder to be created.
 */
public interface CodecSupplier<Encoder extends AudioEncoder, Decoder extends AudioDecoder> {

    /**
     * Creates a new audio encoder with the specified parameters.
     *
     * @param sampleRate The sample rate of the audio data.
     * @param stereo     {@code true} if the audio is in stereo format, {@code false} for mono.
     * @param bufferSize The size of the encoding buffer.
     * @param mtuSize    The maximum transmission unit (MTU) size for network communication.
     * @param codecInfo  The codec information specifying the codec to be used.
     * @return An instance of the audio encoder with the specified parameters.
     */
    @NotNull Encoder createEncoder(
            int sampleRate,
            boolean stereo,
            int bufferSize,
            int mtuSize,
            @NotNull CodecInfo codecInfo
    );

    /**
     * Creates a new audio decoder with the specified parameters.
     *
     * @param sampleRate The sample rate of the audio data.
     * @param stereo     {@code true} if the audio is in stereo format, {@code false} for mono.
     * @param bufferSize The size of the decoding buffer.
     * @param mtuSize    The maximum transmission unit (MTU) size for network communication.
     * @param codecInfo  The codec information specifying the codec to be used.
     * @return An instance of the audio decoder with the specified parameters.
     */
    @NotNull Decoder createDecoder(
            int sampleRate,
            boolean stereo,
            int bufferSize,
            int mtuSize,
            @NotNull CodecInfo codecInfo
    );

    /**
     * Retrieves the name of the codec.
     *
     * @return The name of the codec.
     */
    @NotNull String getName();
}
