package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.proto.data.capture.Activation;

public interface ClientActivation extends Activation {

    Type getType();

    KeyBinding getPttKey();

    KeyBinding getToggleKey();

    void setDisabled(boolean disabled);

    boolean isDisabled();

    boolean isActivated();

    long getLastActivation();

    int getDistance();

    @NotNull Result process(short[] samples);

    enum Type {
        PUSH_TO_TALK,
        VOICE,
        INHERIT
    }

    enum Result {
        NOT_ACTIVATED,
        ACTIVATED,
        END;

        public boolean isActivated() {
            return this == ACTIVATED || this == END;
        }
    }
}
