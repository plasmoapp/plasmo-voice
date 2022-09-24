package su.plo.voice.client.audio.codec.opus;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;
import su.plo.voice.api.audio.codec.CodecException;

public final class JavaOpusDecoder implements BaseOpusDecoder {

    private final int sampleRate;
    private final int channels;
    private final int bufferSize;
    private final int mtuSize;

    private OpusDecoder decoder;
    private short[] buffer;

    public JavaOpusDecoder(int sampleRate, boolean stereo, int bufferSize, int mtuSize) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.bufferSize = bufferSize;
        this.mtuSize = mtuSize;
    }

    @Override
    public short[] decode(byte[] encoded) throws CodecException {
        if (!isOpen()) throw new CodecException("Decoder is not open");

        int result;
        try {
            if (encoded == null || encoded.length == 0) {
                result = decoder.decode(null, 0, 0, buffer, 0, bufferSize, false);
            } else {
                result = decoder.decode(encoded, 0, encoded.length, buffer, 0, bufferSize, false);
            }
        } catch (OpusException e) {
            throw new CodecException("Failed to decode audio", e);
        }

        short[] decoded = new short[result];
        System.arraycopy(buffer, 0, decoded, 0, result);

        return decoded;
    }

    @Override
    public void open() throws CodecException {
        try {
            this.decoder = new OpusDecoder(sampleRate, channels);
            this.buffer = new short[mtuSize];
        } catch (OpusException e) {
            throw new CodecException("Failed to open opus decoder", e);
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        decoder.resetState();
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        this.decoder = null;
        this.buffer = null;
    }

    @Override
    public boolean isOpen() {
        return decoder != null;
    }
}
