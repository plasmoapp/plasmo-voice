package su.plo.voice.encryption;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.encryption.EncryptionSupplier;
import su.plo.voice.api.util.Params;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class VoiceEncryptionManager implements EncryptionManager {

    private final Map<String, EncryptionSupplier> algorithms = Maps.newHashMap();

    @Override
    public synchronized @NotNull Encryption create(@NotNull String name, @NotNull Params params) {
        checkNotNull(name, "name cannot be null");
        checkNotNull(params, "params cannot be null");

        EncryptionSupplier supplier = algorithms.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Encryption algorithm with name " + name + " is not registered");
        }

        return supplier.create(params);
    }

    @Override
    public synchronized void register(@NotNull EncryptionSupplier supplier) {
        String name = supplier.getName();

        checkNotNull(name, "name cannot be null");
        checkNotNull(supplier, "supplier cannot be null");

        if (algorithms.containsKey(name)) {
            throw new IllegalArgumentException("Encryption algorithm with name " + name + " is already exist");
        }

        algorithms.put(name, supplier);
    }

    @Override
    public synchronized boolean unregister(@NotNull String name) {
        checkNotNull(name, "name cannot be null");
        return algorithms.remove(name) != null;
    }

    @Override
    public synchronized boolean unregister(@NotNull EncryptionSupplier supplier) {
        String name = supplier.getName();
        checkNotNull(name, "name cannot be null");
        return algorithms.remove(name) != null;
    }

    @Override
    public synchronized Collection<EncryptionSupplier> getAlgorithms() {
        return algorithms.values();
    }
}
