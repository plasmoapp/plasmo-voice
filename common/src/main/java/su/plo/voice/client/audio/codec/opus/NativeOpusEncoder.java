package su.plo.voice.client.audio.codec.opus;

import com.sun.jna.ptr.PointerByReference;
import su.plo.voice.api.audio.codec.CodecException;
import tomp2p.opuswrapper.Opus;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public final class NativeOpusEncoder implements BaseOpusEncoder {

    private final int sampleRate;
    private final int bufferSize;
    private final int channels;
    private final int application;

    private PointerByReference encoder;
    private ByteBuffer buffer;

    public NativeOpusEncoder(int sampleRate, boolean stereo, int bufferSize, int application) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.bufferSize = bufferSize;
        this.application = application;
    }

    @Override
    public byte[] encode(short[] samples) throws CodecException {
        if (!isOpen()) throw new CodecException("Encoder is not open");

        ShortBuffer shortSamples = ShortBuffer.wrap(samples);

        buffer.clear();
        int result = Opus.INSTANCE.opus_encode(encoder, shortSamples, bufferSize, buffer, samples.length);

        if (result < 0) throw new CodecException("Failed to encode audio: " + result);

        byte[] encoded = new byte[result];
        buffer.get(encoded);

        return encoded;
    }

    @Override
    public void open() throws CodecException {
        IntBuffer error = IntBuffer.allocate(1);
        this.encoder = Opus.INSTANCE.opus_encoder_create(sampleRate, channels, application, error);
        this.buffer = ByteBuffer.allocate(4096);

        if (error.get() != Opus.OPUS_OK && encoder == null) {
            throw new CodecException("Failed to open opus encoder:" + error.get());
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.INSTANCE.OPUS_RESET_STATE);
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        Opus.INSTANCE.opus_encoder_destroy(encoder);
        this.encoder = null;
        this.buffer = null;
    }

    @Override
    public boolean isOpen() {
        return encoder != null;
    }

    @Override
    public void setBitrate(int bitrate) {
        if (!isOpen()) return;

        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_SET_BITRATE_REQUEST, bitrate);
    }

    @Override
    public int getBitrate() {
        IntBuffer request = IntBuffer.allocate(1);
        Opus.INSTANCE.opus_encoder_ctl(encoder, Opus.OPUS_GET_BITRATE_REQUEST, request);

        return request.get();
    }
}
