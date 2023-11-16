package su.plo.voice.audio.codec.opus;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;
import org.concentus.OpusException;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.proto.data.audio.codec.opus.OpusMode;

public final class JavaOpusEncoder implements BaseOpusEncoder {

    private final int sampleRate;
    private final int channels;
    private final int mtuSize;

    private OpusApplication application;
    private OpusEncoder encoder;
    private byte[] buffer;

    public JavaOpusEncoder(
            int sampleRate,
            boolean stereo,
            OpusMode opusMode,
            int mtuSize
    ) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.mtuSize = mtuSize;

        setApplication(opusMode);
    }

    @Override
    public byte[] encode(short[] samples) throws CodecException {
        if (!isOpen()) throw new CodecException("Encoder is not open");

        int result;
        try {
            result = encoder.encode(samples, 0, samples.length / channels, buffer, 0, mtuSize);
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
            this.encoder = new OpusEncoder(sampleRate, channels, application);
            this.buffer = new byte[mtuSize];
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

    private void setApplication(OpusMode opusMode) {
        if (opusMode == OpusMode.VOIP) {
            this.application = OpusApplication.OPUS_APPLICATION_VOIP;
        } else if (opusMode == OpusMode.AUDIO) {
            this.application = OpusApplication.OPUS_APPLICATION_AUDIO;
        } else if (opusMode == OpusMode.RESTRICTED_LOWDELAY) {
            this.application = OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
        }
    }

    @Override
    public void setBitrate(int bitrate) {
        if (!isOpen()) return;

        encoder.setBitrate(bitrate);
    }

    @Override
    public int getBitrate() {
        if (!isOpen()) return -1;

        return encoder.getBitrate();
    }
}
