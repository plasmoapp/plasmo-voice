package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.proto.data.audio.capture.Activation;

public interface ClientActivation extends Activation {

    Type getType();

    KeyBinding getPttKey();

    KeyBinding getToggleKey();

    KeyBinding getDistanceIncreaseKey();

    KeyBinding getDistanceDecreaseKey();

    /**
     * Sets the activation's disabled state
     *
     * If activation is disabled, it always returns {@link Result#NOT_ACTIVATED} in
     * the {@link ClientActivation#process(short[])} method
     *
     * @param disabled
     */
    void setDisabled(boolean disabled);

    /**
     * Checks if activation is disabled by toggle or manually by {@link ClientActivation#setDisabled(boolean)}
     *
     * @return true if activation is disabled
     */
    boolean isDisabled();

    /**
     * Checks if activation is activated
     *
     * @return true if activation is activated
     */
    boolean isActivated();

    long getLastActivation();

    int getDistance();

    @NotNull Result process(short[] samples);

    void reset();

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
