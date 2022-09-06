package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.encryption.Encryption;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Audio capture thread
 *
 *
 */
public interface AudioCapture {

    Optional<AudioEncoder> getEncoder();

    void setEncoder(@Nullable AudioEncoder encoder);

    Optional<Encryption> getEncryption();

    void setEncryption(@Nullable Encryption encryption);

    @NotNull Collection<ClientActivation> getActivations();

    Optional<ClientActivation> getActivationById(@NotNull UUID activationId);

    void registerActivation(@NotNull ClientActivation activation);

    void unregisterActivation(@NotNull ClientActivation activation);

    void unregisterActivation(@NotNull UUID activationId);

    void initialize(@NotNull ServerInfo serverInfo);

    /**
     * Opens the input device specified in the config
     *
     * @param format format which the device will be opened
     *               if null current ServerInfo voice format will be used
     *
     * @return {@link InputDevice}
     */
    InputDevice openInputDevice(@Nullable AudioFormat format) throws Exception;

    void start();

    void stop();

    boolean isActive();
}
