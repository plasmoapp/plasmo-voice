package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;

/**
 * A supplier for encryption algorithms.
 */
public interface EncryptionSupplier {

    /**
     * Creates a new encryption instance using the provided key data.
     *
     * @param data The key data used for encryption.
     * @return A new encryption instance.
     */
    @NotNull Encryption create(byte[] data);

    /**
     * Retrieves the name of the encryption algorithm provided by this supplier.
     *
     * @return The name of the encryption algorithm.
     */
    @NotNull String getName();
}
