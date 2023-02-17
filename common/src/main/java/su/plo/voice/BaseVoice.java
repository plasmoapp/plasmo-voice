package su.plo.voice;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.addon.AddonScope;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.client.audio.codec.VoiceCodecManager;
import su.plo.voice.client.audio.codec.opus.OpusCodecSupplier;
import su.plo.voice.encryption.VoiceEncryptionManager;
import su.plo.voice.encryption.aes.AesEncryptionSupplier;
import su.plo.voice.event.VoiceEventBus;
import su.plo.voice.util.version.ModrinthLoader;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class BaseVoice implements PlasmoVoice {

    protected final EventBus eventBus = new VoiceEventBus(this);
    protected final EncryptionManager encryption = new VoiceEncryptionManager();
    protected final CodecManager codecs = new VoiceCodecManager();

    protected final VoiceAddonManager addons = new VoiceAddonManager(this, getScope());

    @Getter
    protected ScheduledExecutorService backgroundExecutor;

    protected BaseVoice() {
        encryption.register(new AesEncryptionSupplier());

        codecs.register(new OpusCodecSupplier());
    }

    protected void onInitialize() {
        this.backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
        eventBus.register(this, this);
    }

    protected void onShutdown() {
        backgroundExecutor.shutdown();
        addons.clear();
    }

    protected void loadAddons() {
        addonsFolder().mkdirs();
        addons.load(ImmutableList.of(modsFolder(), addonsFolder()));
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

    public InputStream getResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    public abstract Logger getLogger();

    protected abstract File modsFolder();

    protected File addonsFolder() {
        return new File(modsFolder(), "plasmovoice");
    }

    protected abstract AddonScope getScope();

    protected abstract ModrinthLoader getLoader();
}
