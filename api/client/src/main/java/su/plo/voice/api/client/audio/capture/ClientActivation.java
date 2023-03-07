package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.Optional;

public interface ClientActivation extends Activation {

    Type getType();

    KeyBinding getPttKey();

    KeyBinding getToggleKey();

    KeyBinding getDistanceIncreaseKey();

    KeyBinding getDistanceDecreaseKey();

    Optional<AudioEncoder> getMonoEncoder();

    Optional<AudioEncoder> getStereoEncoder();

    /**
     * Sets the activation's disabled state
     *
     * If activation is disabled, it always returns {@link Result#NOT_ACTIVATED} in
     * the {@link ClientActivation#process(short[], Result)} method
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
     * Checks if activation is active
     *
     * @return true if activation is active
     */
    boolean isActive();

    long getLastActivation();

    int getDistance();

    @NotNull Result process(short[] samples, @Nullable Result result);

    void reset();

    void closeEncoders();

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
