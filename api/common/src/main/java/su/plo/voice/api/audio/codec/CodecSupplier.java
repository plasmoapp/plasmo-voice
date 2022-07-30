package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

public interface CodecSupplier<Encoder extends AudioEncoder<?, ?>, Decoder extends AudioDecoder<?, ?>> {

    @NotNull Encoder createEncoder(@NotNull Params params);

    @NotNull Decoder createDecoder(@NotNull Params params);
}
