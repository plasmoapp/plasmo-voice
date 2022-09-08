package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

public interface CodecSupplier<Encoder extends AudioEncoder, Decoder extends AudioDecoder> {

    @NotNull Encoder createEncoder(int sampleRate, boolean stereo, @NotNull Params params);

    @NotNull Decoder createDecoder(int sampleRate, boolean stereo, @NotNull Params params);

    @NotNull String getName();
}
