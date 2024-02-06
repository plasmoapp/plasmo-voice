package su.plo.voice;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.logging.DebugLogger;
import su.plo.voice.audio.codec.VoiceCodecManager;
import su.plo.voice.audio.codec.opus.OpusCodecSupplier;
import su.plo.voice.encryption.VoiceEncryptionManager;
import su.plo.voice.encryption.aes.AesEncryptionSupplier;
import su.plo.voice.event.VoiceEventBus;
import su.plo.voice.util.version.ModrinthLoader;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class BaseVoice implements PlasmoVoice {

    public static final Logger LOGGER = LoggerFactory.getLogger("PlasmoVoice");
    public static final DebugLogger DEBUG_LOGGER = new DebugLogger(LOGGER);

    protected final ModrinthLoader loader;

    protected final EventBus eventBus = new VoiceEventBus(this);
    protected final EncryptionManager encryption = new VoiceEncryptionManager();
    protected final CodecManager codecs = new VoiceCodecManager();

    protected final VoiceAddonManager addons;

    @Getter
    protected ScheduledExecutorService backgroundExecutor;

    protected BaseVoice(@NotNull ModrinthLoader loader) {
        this.loader = loader;
        this.addons = new VoiceAddonManager(this);

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

    @Override
    public @NotNull String getVersion() {
        return BuildConstants.VERSION;
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
}
