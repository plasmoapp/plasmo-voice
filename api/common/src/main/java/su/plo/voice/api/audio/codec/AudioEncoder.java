package su.plo.voice.api.audio.codec;

/**
 * Represents an audio encoder for encoding audio.
 */
public interface AudioEncoder {

    /**
     * Encodes the given audio samples into a compressed format represented as an array of bytes.
     *
     * @param samples The audio samples to encode.
     * @return An array of bytes containing the encoded audio data.
     * @throws CodecException If there's an error during the encoding process.
     */
    byte[] encode(short[] samples) throws CodecException;

    /**
     * Opens the audio encoder, preparing it for encoding operations.
     * This method should be called before encoding audio data.
     *
     * @throws CodecException If there's an error while opening the encoder.
     */
    void open() throws CodecException;

    /**
     * Resets the audio encoder to its initial state.
     */
    void reset();

    /**
     * Closes the audio encoder, releasing any allocated resources.
     */
    void close();

    /**
     * Checks if the audio encoder is currently open and ready for encoding operations.
     *
     * @return {@code  true} if the encoder is open, {@code false} otherwise.
     */
    boolean isOpen();
}
