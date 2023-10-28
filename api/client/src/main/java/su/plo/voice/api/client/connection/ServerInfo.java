package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.line.SourceLine;

import javax.sound.sampled.AudioFormat;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents server information of the connected server.
 */
public interface ServerInfo {

    /**
     * Gets the server's unique identifier.
     *
     * @return The server's unique identifier.
     */
    @NotNull UUID getServerId();

    /**
     * Gets the server's secret.
     *
     * <p>
     *     This secret is used to identify client in UDP connection.
     * </p>
     *
     * @return The server's secret.
     */
    @NotNull UUID getSecret();

    /**
     * Gets the server's remote address.
     *
     * @return The remote address of the server.
     */
    @NotNull InetSocketAddress getRemoteAddress();

    /**
     * Gets the server's voice information.
     *
     * @return The {@link VoiceInfo} associated with the server.
     */
    @NotNull VoiceInfo getVoiceInfo();

    /**
     * Gets the player-related information received from the server.
     *
     * @return The {@link PlayerInfo} associated with the server.
     */
    @NotNull PlayerInfo getPlayerInfo();

    /**
     * Gets the server's encryption details if available.
     *
     * @return An optional containing the server's encryption information, or empty if the server has disabled encryption.
     */
    Optional<Encryption> getEncryption();

    /**
     * Creates a new Opus encoder based on server {@link VoiceInfo}.
     *
     * @param stereo Whether the encoder should be stereo.
     * @return An opus {@link AudioEncoder}.
     */
    @NotNull AudioEncoder createOpusEncoder(boolean stereo);

    /**
     * Creates a new Opus decoder based on server {@link VoiceInfo}.
     *
     * @param stereo Whether the decoder should be stereo.
     * @return An opus {@link AudioEncoder}.
     */
    @NotNull AudioDecoder createOpusDecoder(boolean stereo);

    /**
     * Represents voice-related information of the connected server.
     */
    interface VoiceInfo {

        /**
         * Creates an audio format with a fixed sample size of 16 bits.
         *
         * @param stereo Whether the audio is in stereo or mono.
         * @return An {@link AudioFormat}.
         */
        @NotNull AudioFormat createFormat(boolean stereo);

        /**
         * Gets the 20ms frame size (in shorts) based on sample rate.
         *
         * @return The frame size.
         */
        int getFrameSize();

        /**
         * Gets the voice capture information.
         *
         * @return The CaptureInfo for voice capture.
         */
        @NotNull CaptureInfo getCaptureInfo();

        /**
         * Gets the voice source lines.
         *
         * @return A collection of {@link SourceLine} objects.
         */
        @NotNull Collection<SourceLine> getSourceLines();

        /**
         * Gets the voice activations.
         *
         * @return A collection of {@link Activation} objects.
         */
        @NotNull Collection<Activation> getActivations();
    }

    /**
     * Represents player-related information of the connected server.
     */
    interface PlayerInfo {

        /**
         * Gets the player's permission.
         * <p>
         *     Permissions are synced from the server.
         *     Which permissions will be synchronized is changed in the server Plasmo Voice API.
         *     By default, there is only one permission: {@code pv.allow_freecam}
         * </p>
         *
         * @param key The permission key to check.
         * @return An optional containing the permission, or empty if not present.
         */
        Optional<Boolean> get(@NotNull String key);
    }
}
