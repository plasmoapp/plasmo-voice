package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.encryption.Encryption;

import java.util.Optional;

/**
 * Audio capture thread
 *
 *
 */
public interface AudioCapture {

    Optional<AudioEncoder<?, ?>> getEncoder();

    void setEncoder(@Nullable AudioEncoder<byte[], short[]> encoder);

    Optional<Encryption> getEncryption();

    void setEncryption(@Nullable Encryption encryption);

    Optional<InputDevice> getDevice();

    void setDevice(@NotNull InputDevice device);

    @NotNull Activation getActivation();

    void setActivation(@NotNull Activation activation);

    void start();

    void stop();

    interface Activation {
        @NotNull Result process(short[] samples);

        @NotNull String getType();

        void setDisabled(boolean disabled);

        boolean isDisabled();

        boolean isActive();

        boolean isActivePriority();

        long getLastSpeak();

        enum Result {
            NOT_ACTIVATED,
            ACTIVATED,
            END
        }
    }
}
