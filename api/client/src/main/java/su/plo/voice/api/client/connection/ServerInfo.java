package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
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
 * Represents a server info of the connected server
 */
public interface ServerInfo {

    /**
     * Gets the server's id
     */
    @NotNull UUID getServerId();

    /**
     * Gets the server's secret
     *
     * @return the secret
     */
    @NotNull UUID getSecret();

    /**
     * Gets the server's remote address
     *
     * @return {@link InetSocketAddress}
     */
    @NotNull InetSocketAddress getRemoteAddress();

    /**
     * Gets the server's {@link VoiceInfo}
     *
     * @return {@link VoiceInfo}
     */
    @NotNull VoiceInfo getVoiceInfo();

    /**
     * Gets the {@link PlayerInfo}
     *
     * @return {@link PlayerInfo}
     */
    @NotNull PlayerInfo getPlayerInfo();

    /**
     * Gets the server's {@link Encryption}
     *
     * @return {@link Encryption}
     */
    Optional<Encryption> getEncryption();

    /**
     * Creates a new opus encoder
     * <br/>
     * params will be created from {@link VoiceInfo}
     */
    @NotNull AudioEncoder createOpusEncoder(boolean stereo);

    /**
     * Creates a new opus decoder
     * <br/>
     * params will be created from {@link VoiceInfo}
     */
    @NotNull AudioEncoder createOpusDecoder(boolean stereo);

    /**
     * Represents a voice info of the connected server
     */
    interface VoiceInfo {

        /**
         * Gets the audio format based on sample rate
         *
         * sampleSizeInBits is 16
         *
         * @return {@link AudioFormat}
         */
        @NotNull AudioFormat getFormat(boolean stereo);

        /**
         * Gets the buffer size (for shorts) based on sample rate
         *
         * @return the buffer size
         */
        int getBufferSize();

        /**
         * Gets the voice capture info
         *
         * @return the capture info
         */
        @NotNull CaptureInfo getCapture();

        /**
         * Gets the voice source lines
         */
        Collection<SourceLine> getSourceLines();

        /**
         * Gets the voice activations
         */
        Collection<Activation> getActivations();
    }

    /**
     * Represents a player-based info of the connected server
     */
    interface PlayerInfo {

        /**
         * Gets the player's voice permission
         *
         * @param key permission key
         *
         * @return the voice permission
         */
        Optional<Boolean> get(@NotNull String key);
    }
}
