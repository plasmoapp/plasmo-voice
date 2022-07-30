package su.plo.voice;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.event.VoiceEventBus;

import java.io.File;

public abstract class VoiceBase implements PlasmoVoice {

    private final AddonManager addons = new VoiceAddonManager(
            ImmutableList.of(modsFolder(), addonsFolder())
    );
    private final EventBus eventBus = new VoiceEventBus();

    @Override
    public @NotNull AddonManager getAddonManager() {
        return addons;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return eventBus;
    }

    protected abstract File configFolder();

    protected abstract File modsFolder();

    protected abstract File addonsFolder();
}
