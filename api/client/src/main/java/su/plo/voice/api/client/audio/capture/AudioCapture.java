package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.encryption.Encryption;

import java.util.Optional;

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

    void initialize(@NotNull ServerInfo serverInfo);

    void start();

    void stop();

    boolean isActive();
}
