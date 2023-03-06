package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.Collection;

public interface CodecManager {

    @NotNull <T extends AudioEncoder> T createEncoder(@NotNull CodecInfo codecInfo,
                                                      int sampleRate,
                                                      boolean stereo,
                                                      int bufferSize,
                                                      int mtuSize);

    @NotNull <T extends AudioDecoder> T createDecoder(@NotNull CodecInfo codecInfo,
                                                      int sampleRate,
                                                      boolean stereo,
                                                      int bufferSize,
                                                      int mtuSize);

    void register(@NotNull CodecSupplier<?, ?> supplier);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull CodecSupplier<?, ?> supplier);

    Collection<CodecSupplier<?, ?>> getCodecs();
}
