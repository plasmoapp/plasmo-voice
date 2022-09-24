package su.plo.voice.client.audio.codec.opus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.CodecSupplier;
import su.plo.voice.api.util.Params;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpusCodecSupplier implements CodecSupplier<BaseOpusEncoder, BaseOpusDecoder> {

    private static final Logger LOGGER = LogManager.getLogger(OpusCodecSupplier.class);

    @Override
    public @NotNull BaseOpusEncoder createEncoder(int sampleRate, boolean stereo, @NotNull Params params) {
        checkNotNull(params, "params cannot be null");
        int bufferSize = params.get("bufferSize", Integer.class);
        int application = applicationToMode(params.get("mode", String.class));
        int bitrate = validateBitrate(params.get("bitrate", String.class));

        BaseOpusEncoder encoder = new NativeOpusEncoder(sampleRate, stereo, bufferSize, application);
        try {
            encoder.open();
        } catch (Exception e) {
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
            try {
                encoder = new JavaOpusEncoder(sampleRate, stereo, bufferSize, application);
                encoder.open();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to open java opus encoder", e);
            }
        }

        encoder.setBitrate(bitrate);
        LOGGER.info("Opus encoder bitrate is {}", encoder.getBitrate());

        return encoder;
    }

    @Override
    public @NotNull BaseOpusDecoder createDecoder(int sampleRate, boolean stereo, @NotNull Params params) {
        checkNotNull(params, "params cannot be null");
        int bufferSize = params.get("bufferSize", Integer.class);

        BaseOpusDecoder decoder = new NativeOpusDecoder(sampleRate, stereo, bufferSize);
        try {
            decoder.open();
        } catch (Exception e) {
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
            try {
                decoder = new JavaOpusDecoder(sampleRate, stereo, bufferSize);
                decoder.open();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to open java opus encoder", e);
            }
        }

        return decoder;
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
