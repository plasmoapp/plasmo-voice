package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

public interface EncryptionSupplier {
    @NotNull Encryption create(@NotNull Params params);

    @NotNull String getName();
}
