package su.plo.voice.api.encryption;

public interface Encryption {
    byte[] encrypt(byte[] data) throws EncryptionException;

    byte[] decrypt(byte[] data) throws EncryptionException;
}
