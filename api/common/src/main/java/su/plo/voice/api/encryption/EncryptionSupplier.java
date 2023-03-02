package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;

public interface EncryptionSupplier {

    @NotNull Encryption create(byte[] data);

    @NotNull String getName();
}
