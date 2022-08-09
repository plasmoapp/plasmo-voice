package su.plo.voice.client.audio.capture;

import lombok.Getter;
import su.plo.voice.api.client.audio.capture.AudioCapture;

public abstract class BaseActivation implements AudioCapture.Activation {

    @Getter
    protected boolean disabled = false;
    @Getter
    protected boolean active = false;
    @Getter
    protected boolean activePriority = false;
    @Getter
    protected long lastSpeak;

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.active = false;
        this.activePriority = false;
    }
}
