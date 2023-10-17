package su.plo.voice.api.audio.codec;

/**
 * Represents an audio decoder for decoding audio.
 */
public interface AudioDecoder {

    /**
     * Decodes the given encoded audio data into an array of audio samples.
     *
     * @param encoded The encoded audio data to decode.
     * @return An array of audio samples represented as shorts.
     * @throws CodecException If there's an error during the decoding process.
     */
    short[] decode(byte[] encoded) throws CodecException;

    /**
     * Opens the audio decoder.
     * This method should be called before decoding audio data.
     *
     * @throws CodecException If there's an error while opening the decoder.
     */
    void open() throws CodecException;

    /**
     * Resets the audio decoder to its initial state.
     */
    void reset();

    /**
     * Closes the audio decoder, releasing any allocated resources.
     */
    void close();

    /**
     * Checks if the audio decoder is currently open and ready for decoding.
     *
     * @return {@code true} if the decoder is open, {@code false} otherwise.
     */
    boolean isOpen();
}
