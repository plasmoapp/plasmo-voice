package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

// todo: doc
public interface EncryptionManager {
    @NotNull Encryption create(@NotNull String name, @NotNull Params params);

    void register(@NotNull String name, @NotNull EncryptionSupplier supplier);

    boolean unregister(@NotNull String name);
}
