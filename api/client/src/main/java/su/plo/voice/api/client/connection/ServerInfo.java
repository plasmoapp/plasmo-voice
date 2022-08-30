package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.proto.data.capture.Activation;

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
     * Represents a voice info of the connected server
     */
    interface VoiceInfo {

        /**
         * Gets the voice sample rate
         *
         * @return the sample rate
         */
        int getSampleRate();

        /**
         * Gets the voice codec
         *
         * @return the codec
         */
        @Nullable String getCodec();

        /**
         * Gets the proximity activation
         */
        @NotNull Activation getProximityActivation();

        /**
         * Gets the voice activations
         */
        Collection<Activation> getActivations();

        /**
         * Gets the fade divisor
         * todo: move to source info?
         *
         * @return the fade divisor
         */
        int getFadeDivisor();
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
        Optional<Integer> get(@NotNull String key);
    }
}
