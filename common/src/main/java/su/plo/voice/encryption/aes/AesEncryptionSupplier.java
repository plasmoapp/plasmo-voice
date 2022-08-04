package su.plo.voice.encryption.aes;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionSupplier;
import su.plo.voice.api.util.Params;

public final class AesEncryptionSupplier implements EncryptionSupplier {
    @Override
    public @NotNull Encryption create(@NotNull Params params) {
        byte[] key = params.get("key");
        return new AesEncryption(key);
    }

    @Override
    public @NotNull String getName() {
        return "AES_CBC_PKCS5Padding";
    }
}
