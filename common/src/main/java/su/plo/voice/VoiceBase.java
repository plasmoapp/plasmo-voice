package su.plo.voice;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.audio.codec.VoiceCodecManager;
import su.plo.voice.audio.codec.opus.OpusCodecSupplier;
import su.plo.voice.encryption.AesEncryption;
import su.plo.voice.encryption.VoiceEncryptionManager;
import su.plo.voice.event.VoiceEventBus;

import java.io.File;

public abstract class VoiceBase implements PlasmoVoice {

    private final AddonManager addons = new VoiceAddonManager(
            ImmutableList.of(modsFolder(), addonsFolder())
    );
    private final EventBus eventBus = new VoiceEventBus();
    private final EncryptionManager encryption = new VoiceEncryptionManager();
    private final CodecManager codecs = new VoiceCodecManager();

    protected VoiceBase() {
        encryption.register("AES_CBC_PKCS5Padding", (params) -> {
            byte[] key = params.get("key");
            return new AesEncryption(key);
        });

        codecs.register(new OpusCodecSupplier());
    }

    @Override
    public @NotNull AddonManager getAddonManager() {
        return addons;
    }

    @Override
    public @NotNull EncryptionManager getEncryptionManager() {
        return encryption;
    }

    @Override
    public @NotNull CodecManager getCodecManager() {
        return codecs;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return eventBus;
    }

    protected abstract File configFolder();

    protected abstract File modsFolder();

    protected abstract File addonsFolder();
}
