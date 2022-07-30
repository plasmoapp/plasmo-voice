package su.plo.voice.encryption;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.encryption.EncryptionSupplier;
import su.plo.voice.api.util.Params;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class VoiceEncryptionManager implements EncryptionManager {

    private final Map<String, EncryptionSupplier> algorithms = Maps.newHashMap();

    @Override
    public synchronized @NotNull Encryption create(@NotNull String name, @NotNull Params params) {
        checkNotNull(name, "name cannot be null");
        checkNotNull(params, "params cannot be null");

        EncryptionSupplier encryption = algorithms.get(name);
        if (encryption == null) {
            throw new IllegalArgumentException("Encryption algorithm with name " + name + " is not registered");
        }

        return encryption.supply(params);
    }

    @Override
    public synchronized void register(@NotNull String name, @NotNull EncryptionSupplier supplier) {
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
}
