package su.plo.voice.client.audio.codec.opus;

import com.sun.jna.ptr.PointerByReference;
import su.plo.voice.api.audio.codec.CodecException;
import tomp2p.opuswrapper.Opus;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class NativeOpusDecoder implements BaseOpusDecoder {

    private final int sampleRate;
    private final int channels;
    private final int bufferSize;

    private PointerByReference decoder;
    private ShortBuffer buffer;

    public NativeOpusDecoder(int sampleRate, boolean stereo, int bufferSize) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.bufferSize = bufferSize;
    }

    @Override
    public short[] decode(byte[] encoded) throws CodecException {
        if (!isOpen()) throw new CodecException("Decoder is not open");

        buffer.clear();
        int result;
        if (encoded == null || encoded.length == 0) {
            result = Opus.INSTANCE.opus_decode(decoder, null, 0, buffer, bufferSize * channels, 0);
        } else {
            result = Opus.INSTANCE.opus_decode(decoder, encoded, encoded.length, buffer, bufferSize * channels, 0);
        }

        if (result < 0) throw new CodecException("Failed to decode audio: " + result);

        short[] decoded = new short[result * channels];
        for (int channel = 0; channel < channels; channel++) {
            buffer.get(decoded, result * channel, result);
        }

        return decoded;
    }

    @Override
    public void open() throws CodecException {
        IntBuffer error = IntBuffer.allocate(1);
        this.decoder = Opus.INSTANCE.opus_decoder_create(sampleRate, channels, error);
        this.buffer = ShortBuffer.allocate(4096);

        if (error.get() != Opus.OPUS_OK && decoder == null) {
            throw new CodecException("Failed to open opus decoder:" + error.get());
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        Opus.INSTANCE.opus_decoder_ctl(decoder, Opus.INSTANCE.OPUS_RESET_STATE);
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        Opus.INSTANCE.opus_decoder_destroy(decoder);
        this.decoder = null;
        this.buffer = null;
    }

    @Override
    public boolean isOpen() {
        return decoder != null;
    }
}
