package su.plo.voice.client.audio.codec;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.audio.codec.CodecSupplier;
import su.plo.voice.api.util.Params;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceCodecManager implements CodecManager {

    private final Map<String, CodecSupplier<?, ?>> codecs = Maps.newHashMap();

    @Override
    public synchronized @NotNull <T extends AudioEncoder> T createEncoder(@NotNull String name,
                                                                          int sampleRate,
                                                                          boolean stereo,
                                                                          @NotNull Params params) {
        checkNotNull(name, "name cannot be null");
        checkNotNull(params, "params cannot be null");

        CodecSupplier<?, ?> supplier = codecs.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Codec encoder with name " + name + " is not registered");
        }

        return (T) supplier.createEncoder(sampleRate, stereo, params);
    }

    @Override
    public synchronized @NotNull <T extends AudioDecoder> T createDecoder(@NotNull String name,
                                                                          int sampleRate,
                                                                          boolean stereo,
                                                                          @NotNull Params params) {
        checkNotNull(name, "name cannot be null");
        checkNotNull(params, "params cannot be null");

        CodecSupplier<?, ?> supplier = codecs.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Codec encoder with name " + name + " is not registered");
        }

        return (T) supplier.createDecoder(sampleRate, stereo, params);
    }

    @Override
    public synchronized void register(@NotNull CodecSupplier<?, ?> supplier) {
        String name = supplier.getName();

        checkNotNull(name, "name cannot be null");
        checkNotNull(supplier, "supplier cannot be null");

        if (codecs.containsKey(name)) {
            throw new IllegalArgumentException("Codec with name " + name + " is already exist");
        }

        codecs.put(name, supplier);
    }

    @Override
    public synchronized boolean unregister(@NotNull String name) {
        checkNotNull(name, "name cannot be null");
        return codecs.remove(name) != null;
    }

    @Override
    public synchronized boolean unregister(@NotNull CodecSupplier<?, ?> supplier) {
        checkNotNull(supplier, "supplier cannot be null");
        checkNotNull(supplier.getName(), "name cannot be null");
        return codecs.remove(supplier.getName()) != null;
    }

    @Override
    public synchronized Collection<CodecSupplier<?, ?>> getCodecs() {
        return codecs.values();
    }
}
