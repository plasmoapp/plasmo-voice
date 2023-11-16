package su.plo.voice.audio.codec.opus;

import com.plasmoverse.opus.OpusEncoder;
import com.plasmoverse.opus.OpusException;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.proto.data.audio.codec.opus.OpusMode;

import java.io.IOException;
import java.util.Arrays;

public final class NativeOpusEncoder implements BaseOpusEncoder {

    private final int sampleRate;
    private final int channels;
    private final OpusMode opusMode;
    private final int mtuSize;

    private OpusEncoder encoder;

    public NativeOpusEncoder(
            int sampleRate,
            boolean stereo,
            OpusMode opusMode,
            int mtuSize
    ) {
        this.sampleRate = sampleRate;
        this.channels = stereo ? 2 : 1;
        this.opusMode = opusMode;
        this.mtuSize = mtuSize;
    }

    @Override
    public byte[] encode(short[] samples) throws CodecException {
        if (!isOpen()) throw new CodecException("Encoder is not open");

        try {
            return encoder.encode(samples);
        } catch (OpusException e) {
            throw new CodecException("Failed to encode audio", e);
        }
    }

    @Override
    public void open() throws CodecException {
        try {
            com.plasmoverse.opus.OpusMode mode = Arrays.stream(com.plasmoverse.opus.OpusMode.values())
                    .filter((element) -> element.getApplication() == opusMode.getApplication())
                    .findFirst()
                    .orElseThrow(() -> new CodecException("Invalid opus application mode"));

            this.encoder = OpusEncoder.create(sampleRate, channels == 2, mtuSize, mode);
        } catch (OpusException | IOException e) {
            throw new CodecException("Failed to open opus encoder", e);
        }
    }

    @Override
    public void reset() {
        if (!isOpen()) return;

        encoder.reset();
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        encoder.close();
        this.encoder = null;
    }

    @Override
    public boolean isOpen() {
        return encoder != null;
    }

    @Override
    public void setBitrate(int bitrate) {
        if (!isOpen()) return;

        encoder.setBitrate(bitrate);
    }

    @Override
    public int getBitrate() {
        try {
            return encoder.getBitrate();
        } catch (OpusException e) {
            return 0;
        }
    }
}
