package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

import java.util.Collection;

// todo: doc
public interface EncryptionManager {
    @NotNull Encryption create(@NotNull String name, @NotNull Params params);

    void register(@NotNull EncryptionSupplier supplier);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull EncryptionSupplier supplier);

    Collection<EncryptionSupplier> getAlgorithms();
}
