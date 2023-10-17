package su.plo.voice.api.encryption;

import org.jetbrains.annotations.NotNull;

/**
 * Represents and encryption for encrypting and decrypting data using encryption algorithms.
 */
public interface Encryption {

    /**
     * Encrypts the provided data using an encryption algorithm.
     *
     * @param data The data to be encrypted.
     * @return The encrypted data.
     * @throws EncryptionException If an error occurs during encryption.
     */
    byte[] encrypt(byte[] data) throws EncryptionException;

    /**
     * Decrypts the provided data using the corresponding decryption algorithm.
     *
     * @param data The data to be decrypted.
     * @return The decrypted data.
     * @throws EncryptionException If an error occurs during decryption.
     */
    byte[] decrypt(byte[] data) throws EncryptionException;

    /**
     * Updates the encryption key data.
     *
     * @param keyData The encryption key data.
     */
    void updateKeyData(byte[] keyData);

    /**
     * Retrieves the name of the encryption algorithm.
     *
     * @return The name of the encryption algorithm.
     */
    @NotNull String getName();
}
