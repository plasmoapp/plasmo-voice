package su.plo.voice.encryption.aes;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionSupplier;

public final class AesEncryptionSupplier implements EncryptionSupplier {

    @Override
    public @NotNull Encryption create(byte[] data) {
        return new AesEncryption(data);
    }

    @Override
    public @NotNull String getName() {
        return "AES/CBC/PKCS5Padding";
    }
}
