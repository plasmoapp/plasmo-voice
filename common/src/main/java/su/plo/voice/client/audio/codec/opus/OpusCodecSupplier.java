package su.plo.voice.client.audio.codec.opus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.CodecSupplier;
import su.plo.voice.api.util.Params;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpusCodecSupplier implements CodecSupplier<OpusEncoderBase, OpusDecoderBase> {

    private static final Logger LOGGER = LogManager.getLogger(OpusCodecSupplier.class);

    @Override
    public @NotNull OpusEncoderBase createEncoder(@NotNull Params params) {
        checkNotNull(params, "params cannot be null");
        int sampleRate = params.get("sampleRate", Integer.class);
        int bufferSize = params.get("bufferSize", Integer.class);
        int application = params.get("application", Integer.class);

        OpusEncoderBase encoder = new NativeOpusEncoder(sampleRate, bufferSize, application);
        try {
            encoder.open();
        } catch (Exception e) {
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
            try {
                encoder = new JavaOpusEncoder(sampleRate, bufferSize, application);
                encoder.open();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to open java opus encoder", e);
            }
        }

        return encoder;
    }

    @Override
    public @NotNull OpusDecoderBase createDecoder(@NotNull Params params) {
        checkNotNull(params, "params cannot be null");
        int sampleRate = params.get("sampleRate", Integer.class);
        int bufferSize = params.get("bufferSize", Integer.class);

        OpusDecoderBase decoder = new NativeOpusDecoder(sampleRate, bufferSize);
        try {
            decoder.open();
        } catch (Exception e) {
            LOGGER.warn("Failed to load native opus. Falling back to pure java impl", e);
            try {
                decoder = new JavaOpusDecoder(sampleRate, bufferSize);
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
}
