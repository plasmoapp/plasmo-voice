package su.plo.voice.api;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.audio.codec.CodecManager;
import su.plo.voice.api.encryption.EncryptionManager;
import su.plo.voice.api.event.EventBus;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Base interface for Plasmo Voice API.
 */
public interface PlasmoVoice {

    /**
     * Gets the {@link ScheduledExecutorService}.
     * This executor is used for handling background tasks.
     *
     * @return the {@link ScheduledExecutorService} for background tasks.
     */
    @NotNull ScheduledExecutorService getBackgroundExecutor();

    /**
     * Gets the {@link AddonManager}.
     * The AddonManager is responsible for managing addons.
     *
     * @return the AddonManager instance.
     */
    @NotNull AddonManager getAddonManager();

    /**
     * Gets the {@link EventBus}, which allows subscribing to Plasmo Voice events.
     *
     * @return the EventBus instance.
     */
    @NotNull EventBus getEventBus();

    /**
     * Gets the {@link EncryptionManager}.
     * The EncryptionManager manages encryption algorithms.
     *
     * @return the EncryptionManager instance.
     */
    @NotNull EncryptionManager getEncryptionManager();

    /**
     * Gets the {@link CodecManager}.
     * The CodecManager is responsible for creating codec encoders/decoders and managing custom codec implementations.
     *
     * @return the CodecManager instance.
     */
    @NotNull CodecManager getCodecManager();

    /**
     * Gets the Plasmo Voice version.
     *
     * @return the Plasmo Voice version.
     */
    @NotNull String getVersion();

    /**
     * Gets the Plasmo Voice config folder.
     *
     * @return the folder where Plasmo Voice configurations are stored.
     */
    @NotNull File getConfigFolder();
}
