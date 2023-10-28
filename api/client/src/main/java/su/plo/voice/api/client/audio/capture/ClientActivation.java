package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.Optional;

/**
 * Represents a client activation.
 */
public interface ClientActivation extends Activation {

    /**
     * Gets the type of client activation.
     *
     * @return The type of client activation.
     */
    @NotNull Type getType();

    /**
     * Gets the key binding for Push-to-Talk.
     *
     * @return The key binding for Push-to-Talk.
     */
    @NotNull Hotkey getPttKey();

    /**
     * Gets the key binding for toggling activation.
     * <br>
     * Toggle works only if {@link #getType()} is Voice or Inherit.
     *
     * @return The key binding for toggling activation.
     */
    @NotNull Hotkey getToggleKey();

    /**
     * Gets the key binding for increasing activation distance.
     *
     * @return The key binding for increasing distance.
     */
    @NotNull Hotkey getDistanceIncreaseKey();

    /**
     * Gets the key binding for decreasing activation distance.
     *
     * @return The key binding for decreasing distance.
     */
    @NotNull Hotkey getDistanceDecreaseKey();

    /**
     * Gets the mono audio encoder associated with this activation.
     *
     * @return An optional containing the mono audio encoder, if available; otherwise, an empty optional.
     */
    Optional<AudioEncoder> getMonoEncoder();

    /**
     * Gets the stereo audio encoder associated with this activation.
     *
     * @return An optional containing the stereo audio encoder, if available; otherwise, an empty optional.
     */
    Optional<AudioEncoder> getStereoEncoder();

    /**
     * Sets the activation's disabled state.
     * <br>
     * If activation is disabled, it always returns {@link Result#NOT_ACTIVATED} in the
     * {@link ClientActivation#process(short[], Result)} method.
     *
     * @param disabled Whether the activation is disabled.
     */
    void setDisabled(boolean disabled);

    /**
     * Checks if activation is disabled by toggle or manually using {@link ClientActivation#setDisabled(boolean)}.
     *
     * @return {@code true} if activation is disabled, {@code false} otherwise.
     */
    boolean isDisabled();

    /**
     * Checks if activation is currently active.
     *
     * @return {@code true} if activation is active, {@code false} otherwise.
     */
    boolean isActive();

    /**
     * Gets the timestamp of the last activation.
     *
     * @return The timestamp of the last activation.
     */
    long getLastActivation();

    /**
     * Gets the activation distance.
     *
     * @return The activation distance.
     */
    int getDistance();

    /**
     * Processes audio samples.
     *
     * @param samples The audio samples to process.
     * @param result  The result of the parent activation.
     * @return The result of processing the audio samples.
     */
    @NotNull Result process(short[] samples, @Nullable Result result);

    /**
     * Resets the activation state.
     */
    void reset();

    /**
     * Cleans up and releases resources associated with this activation.
     */
    void cleanup();

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
