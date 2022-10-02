package su.plo.voice;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.client.audio.codec.VoiceCodecManager;
import su.plo.voice.client.audio.codec.opus.OpusCodecSupplier;
import su.plo.voice.encryption.VoiceEncryptionManager;
import su.plo.voice.encryption.aes.AesEncryptionSupplier;
import su.plo.voice.event.VoiceEventBus;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class BaseVoice implements PlasmoVoice {

    protected final AddonManager addons = new VoiceAddonManager(
            this,
            ImmutableList.of(modsFolder(), addonsFolder())
    );
    protected final EventBus eventBus = new VoiceEventBus();
    protected final EncryptionManager encryption = new VoiceEncryptionManager();
    protected final CodecManager codecs = new VoiceCodecManager();

    protected ScheduledExecutorService executor;

    protected BaseVoice() {
        encryption.register(new AesEncryptionSupplier());

        codecs.register(new OpusCodecSupplier());
    }

    protected void onInitialize() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    protected void onShutdown() {
        executor.shutdown();
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

    public abstract File configFolder();

    public abstract InputStream getResource(String name);

    protected abstract Logger getLogger();

    protected abstract File modsFolder();

    protected abstract File addonsFolder();
}
