package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.EncryptionInfo;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a server info of the connected server
 */
public interface ServerInfo {

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
     * Gets the server's {@link EncryptionInfo}
     *
     * @return {@link EncryptionInfo}
     */
    Optional<EncryptionInfo> getEncryptionInfo();

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
         * Gets the voice available distances
         *
         * @return collection of distances
         */
        Collection<Integer> getDistances();

        /**
         * Gets the min distance from a distances collection
         *
         * @return the min distance
         */
        int getMinDistance();

        /**
         * Gets the max distance from a distances collection
         *
         * @return the max distance
         */
        int getMaxDistance();

        /**
         * Gets the max allowed priority distance
         *
         * @return the max priority distance
         */
        int getMaxPriorityDistance();
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
