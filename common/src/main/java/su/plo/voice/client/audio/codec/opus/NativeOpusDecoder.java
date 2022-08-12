package su.plo.voice.client.audio.codec.opus;

import com.sun.jna.ptr.PointerByReference;
import su.plo.voice.api.audio.codec.CodecException;
import tomp2p.opuswrapper.Opus;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class NativeOpusDecoder implements BaseOpusDecoder {

    private final int sampleRate;
    private final int bufferSize;

    private PointerByReference decoder;
    private ShortBuffer buffer;

    public NativeOpusDecoder(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
    }

    @Override
    public short[] decode(byte[] encoded) throws CodecException {
        if (!isOpen()) throw new CodecException("Decoder is not open");

        int result;
        if (encoded == null || encoded.length == 0) {
            result = Opus.INSTANCE.opus_decode(decoder, null, 0, buffer, bufferSize, 0);
        } else {
            result = Opus.INSTANCE.opus_decode(decoder, encoded, encoded.length, buffer, bufferSize, 0);
        }

        if (result < 0) throw new CodecException("Failed to decode audio: " + result);

        short[] decoded = new short[result];
        buffer.get(decoded);

        return decoded;
    }

    @Override
    public void open() throws CodecException {
        IntBuffer error = IntBuffer.allocate(1);
        this.decoder = Opus.INSTANCE.opus_decoder_create(sampleRate, 1, error);
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
