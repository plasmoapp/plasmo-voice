package su.plo.voice.client.audio.codec.opus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.CodecSupplier;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.codec.opus.OpusEncoderInfo;

import java.io.IOException;

public final class OpusCodecSupplier implements CodecSupplier<BaseOpusEncoder, BaseOpusDecoder> {

    private static final Logger LOGGER = LogManager.getLogger(OpusCodecSupplier.class);

    @Override
    public @NotNull BaseOpusEncoder createEncoder(int sampleRate,
                                                  boolean stereo,
                                                  int bufferSize,
                                                  int mtuSize,
                                                  @NotNull CodecInfo codecInfo) {
        OpusEncoderInfo opusEncoderInfo;
        try {
            opusEncoderInfo = new OpusEncoderInfo(codecInfo);
        } catch (IOException e) {
            throw new IllegalStateException("Bad codec info received", e);
        }

        BaseOpusEncoder encoder;
        try {
            Class.forName("su.plo.opus.Opus");

            encoder = new NativeOpusEncoder(sampleRate, stereo, bufferSize, opusEncoderInfo.getMode(), mtuSize);
            encoder.open();
        } catch (ClassNotFoundException ignored) {
            encoder = null;
        } catch (Exception | LinkageError e) {
            encoder = null;
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
        }

        if (encoder == null) {
            try {
                encoder = new JavaOpusEncoder(sampleRate, stereo, bufferSize, opusEncoderInfo.getMode(), mtuSize);
                encoder.open();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to open java opus encoder", e);
            }
        }

        encoder.setBitrate(opusEncoderInfo.getBitrate());
        LOGGER.info("Opus encoder bitrate is {}", encoder.getBitrate());

        return encoder;
    }

    @Override
    public @NotNull BaseOpusDecoder createDecoder(int sampleRate,
                                                  boolean stereo,
                                                  int bufferSize,
                                                  int mtuSize,
                                                  @NotNull CodecInfo codecInfo) {
        BaseOpusDecoder decoder;
        try {
            Class.forName("su.plo.opus.Opus");

            decoder = new NativeOpusDecoder(sampleRate, stereo, bufferSize, mtuSize);
            decoder.open();
            return decoder;
        } catch (ClassNotFoundException ignored) {
        } catch (Exception | LinkageError e) {
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
        }

        try {
            decoder = new JavaOpusDecoder(sampleRate, stereo, bufferSize, mtuSize);
            decoder.open();
            return decoder;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open java opus encoder", e);
        }
    }

    @Override
    public @NotNull String getName() {
        return "opus";
    }

    private int validateBitrate(@NotNull String rawBitrate) {
        try {
            int bitrate = Integer.parseInt(rawBitrate);

            if (bitrate < 0) {
                if (bitrate != -1 && bitrate != -1000) bitrate = -1000;
            } else if (bitrate > 512_000) bitrate = 512_000;

            return bitrate;
        } catch (NumberFormatException ignored) {
            return -1000;
        }
    }

    private int applicationToMode(@NotNull String mode) {
        switch (mode) {
            case "VOIP": return 2048;
            case "AUDIO": return 2049;
            case "RESTRICTED_LOWDELAY": return 2051;
        }

        throw new IllegalArgumentException("Bad opus mode");
    }
}
