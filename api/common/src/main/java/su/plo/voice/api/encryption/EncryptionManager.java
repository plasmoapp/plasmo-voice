package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

// todo: doc
public interface EncryptionManager {

    @NotNull Encryption create(@NotNull String name, byte[] data);

    void register(@NotNull EncryptionSupplier supplier);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull EncryptionSupplier supplier);

    Collection<EncryptionSupplier> getAlgorithms();
}
