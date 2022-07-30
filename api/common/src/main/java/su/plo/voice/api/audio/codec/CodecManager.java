package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

public interface CodecManager {

    @NotNull AudioEncoder<?, ?> createEncoder(@NotNull String name, @NotNull Params params);

    @NotNull AudioDecoder<?, ?> createDecoder(@NotNull String name, @NotNull Params params);

    void register(@NotNull String name, @NotNull CodecSupplier<?, ?> supplier);

    boolean unregister(@NotNull String name);
}
