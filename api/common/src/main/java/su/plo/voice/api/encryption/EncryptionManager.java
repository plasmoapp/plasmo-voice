package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Manages encryption algorithms.
 */
public interface EncryptionManager {

    /**
     * Creates a new encryption instance using the specified encryption algorithm and provided key data.
     *
     * @param name The name of the encryption algorithm.
     * @param data The key data used for encryption.
     * @return A new encryption instance.
     */
    @NotNull Encryption create(@NotNull String name, byte[] data);

    /**
     * Registers an encryption algorithm.
     *
     * @param supplier The encryption algorithm supplier to register.
     */
    void register(@NotNull EncryptionSupplier supplier);

    /**
     * Unregisters an encryption algorithm by its name.
     *
     * @param name The name of the encryption algorithm to unregister.
     * @return {@code true} if the activation was successfully unregistered, {@code false} if the encryption was not found.
     */
    boolean unregister(@NotNull String name);

    /**
     * Unregisters an encryption algorithm supplier.
     *
     * @param supplier The encryption algorithm supplier to unregister.
     * @return {@code true} if the activation was successfully unregistered, {@code false} if the encryption was not found.
     */
    boolean unregister(@NotNull EncryptionSupplier supplier);

    /**
     * Retrieves a collection of registered encryption algorithms.
     *
     * @return A collection of encryption algorithms.
     */
    Collection<EncryptionSupplier> getAlgorithms();
}
