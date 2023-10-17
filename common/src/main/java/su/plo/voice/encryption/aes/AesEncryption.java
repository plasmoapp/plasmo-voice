package su.plo.voice.encryption.aes;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public final class AesEncryption implements Encryption {

    public static final String CIPHER = "AES/CBC/PKCS5Padding";

    private @NotNull SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesEncryption(byte[] keyData) {
        this.key = new SecretKeySpec(keyData, "AES");
    }

    @Override
    public byte[] encrypt(byte[] data) throws EncryptionException {
        try {
            IvParameterSpec iv = generateIv();

            // initialize cipher
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            // encrypt data
            byte[] encrypted = cipher.doFinal(data);

            return copyIvEncrypted(iv, encrypted);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encrypted) throws EncryptionException {
        try {
            IvParameterSpec iv = ivFromEncrypted(encrypted);

            // initialize cipher
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            encrypted = dataFromEncrypted(encrypted);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    @Override
    public void updateKeyData(byte[] keyData) {
        this.key = new SecretKeySpec(keyData, "AES");
    }

    @Override
    public @NotNull String getName() {
        return CIPHER;
    }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Copies iv and encrypted data to new byte array
     *
     * @return the byte array
     */
    private byte[] copyIvEncrypted(IvParameterSpec iv, byte[] encrypted) {
        byte[] encryptedIv = new byte[iv.getIV().length + encrypted.length];

        System.arraycopy(iv.getIV(), 0, encryptedIv, 0, iv.getIV().length);
        System.arraycopy(encrypted, 0, encryptedIv, iv.getIV().length, encrypted.length);

        return encryptedIv;
    }

    private IvParameterSpec ivFromEncrypted(byte[] encrypted) {
        byte[] iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        return new IvParameterSpec(iv);
    }

    private byte[] dataFromEncrypted(byte[] encrypted) {
        byte[] data = new byte[encrypted.length - 16];
        System.arraycopy(encrypted, 16, data, 0, data.length);
        return data;
    }
}
