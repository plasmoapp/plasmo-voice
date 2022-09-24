package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

import java.util.Collection;

public interface CodecManager {

    @NotNull <T extends AudioEncoder> T createEncoder(@NotNull String name,
                                                      int sampleRate,
                                                      boolean stereo,
                                                      int bufferSize,
                                                      int mtuSize,
                                                      @NotNull Params params);

    @NotNull <T extends AudioDecoder> T createDecoder(@NotNull String name,
                                                      int sampleRate,
                                                      boolean stereo,
                                                      int bufferSize,
                                                      int mtuSize,
                                                      @NotNull Params params);

    void register(@NotNull CodecSupplier<?, ?> supplier);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull CodecSupplier<?, ?> supplier);

    Collection<CodecSupplier<?, ?>> getCodecs();
}
