package su.plo.voice.audio.codec.opus;

import com.plasmoverse.opus.OpusDecoder;
import com.plasmoverse.opus.OpusException;
import su.plo.voice.api.audio.codec.CodecException;

import java.io.IOException;
import java.util.Arrays;

public final class NativeOpusDecoder implements BaseOpusDecoder {

    private final int sampleRate;
    private final int channels;
    private final int frameSize;

    private OpusDecoder decoder;

    public NativeOpusDecoder(int sampleRate, boolean stereo, int frameSize) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.frameSize = frameSize;
    }

    @Override
    public short[] decode(byte[] encoded) throws CodecException {
        if (!isOpen()) throw new CodecException("Decoder is not open");

        try {
            return decoder.decode(encoded);
        } catch (OpusException e) {
            throw new CodecException("Failed to decode audio: " + e);
        }
    }

    @Override
    public void open() throws CodecException {
        try {
            this.decoder = OpusDecoder.create(sampleRate, channels == 2, frameSize);
        } catch (OpusException | IOException e) {
            throw new CodecException("Failed to open opus decoder", e);
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        decoder.reset();
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        decoder.close();
        this.decoder = null;
    }

    @Override
    public boolean isOpen() {
        return decoder != null;
    }

    @Override
    public short[] decodePLC() throws CodecException {
        return decode(null);
    }
}
