package su.plo.voice.audio.codec.opus;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import su.plo.voice.api.audio.codec.CodecException;
import tomp2p.opuswrapper.Opus;

public final class JavaOpusEncoder implements OpusEncoderBase {

    private final int sampleRate;
    private final int bufferSize;

    private OpusApplication application;
    private OpusEncoder encoder;
    private byte[] buffer;

    public JavaOpusEncoder(int sampleRate, int bufferSize, int application) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.application = OpusApplication.OPUS_APPLICATION_UNIMPLEMENTED;

        setApplication(application);
    }

    @Override
    public byte[] encode(short[] samples) throws CodecException {
        if (!isOpen()) throw new CodecException("Encoder is not open");

        int result;
        try {
            result = encoder.encode(samples, 0, bufferSize, buffer, 0, buffer.length);
        } catch (OpusException e) {
            throw new CodecException("Failed to encode audio", e);
        }

        byte[] encoded = new byte[result];
        System.arraycopy(buffer, 0, encoded, 0, result);

        return encoded;
    }

    @Override
    public void open() throws CodecException {
        try {
            this.encoder = new OpusEncoder(sampleRate, 1, application);
        } catch (OpusException e) {
            throw new CodecException("Failed to open opus encoder", e);
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        encoder.resetState();
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        this.encoder = null;
        this.buffer = null;
    }

    @Override
    public boolean isOpen() {
        return encoder != null;
    }

    private void setApplication(int application) {
        if (application == Opus.OPUS_APPLICATION_VOIP) {
            this.application = OpusApplication.OPUS_APPLICATION_VOIP;
        } else if (application == Opus.OPUS_APPLICATION_AUDIO) {
            this.application = OpusApplication.OPUS_APPLICATION_AUDIO;
        } else if (application == Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY) {
            this.application = OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
        }
    }
}
