package su.plo.voice.client.render;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;

public abstract class ModRenderer {

    protected final PlasmoVoiceClient voiceClient;

    public ModRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        this.voiceClient = voiceClient;
    }
}
