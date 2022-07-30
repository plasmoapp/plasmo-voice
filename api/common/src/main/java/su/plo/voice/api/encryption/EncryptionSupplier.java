package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;

@FunctionalInterface
public interface EncryptionSupplier {
    @NotNull Encryption supply(@NotNull Params params);
}
