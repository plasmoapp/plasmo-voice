package su.plo.voice.audio.codec;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.audio.codec.CodecSupplier;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unchecked")
public final class VoiceCodecManager implements CodecManager {

    private final Map<String, CodecSupplier<?, ?>> codecs = Maps.newHashMap();

    @Override
    public synchronized @NotNull <T extends AudioEncoder> T createEncoder(
            @NotNull CodecInfo codecInfo,
            int sampleRate,
            boolean stereo,
            int mtuSize
    ) {
        CodecSupplier<?, ?> supplier = codecs.get(codecInfo.getName());
        if (supplier == null) {
            throw new IllegalArgumentException("Codec encoder with name " + codecInfo.getName() + " is not registered");
        }

        return (T) supplier.createEncoder(codecInfo, sampleRate, stereo, mtuSize);
    }

    @Override
    public synchronized @NotNull <T extends AudioDecoder> T createDecoder(
            @NotNull CodecInfo codecInfo,
            int sampleRate,
            boolean stereo,
            int frameSize
    ) {
        CodecSupplier<?, ?> supplier = codecs.get(codecInfo.getName());
        if (supplier == null) {
            throw new IllegalArgumentException("Codec encoder with name " + codecInfo.getName() + " is not registered");
        }

        return (T) supplier.createDecoder(codecInfo, sampleRate, stereo, frameSize);
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
    public synchronized @NotNull Collection<CodecSupplier<?, ?>> getCodecs() {
        return codecs.values();
    }
}
