package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.encryption.EncryptionInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.net.SocketAddress;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a client connection to the server.
 *
 * <p>
 *     The connection is initialized upon receiving the first plasmo:voice/v2 channel packet.
 * </p>
 */
public interface ServerConnection {

    /**
     * Gets the language of the server.
     *
     * @return A map containing language.
     */
    @NotNull Map<String, String> getLanguage();

    /**
     * Gets the remote socket address of the server.
     *
     * @return The remote socket address.
     */
    @NotNull SocketAddress getRemoteAddress();

    /**
     * Gets the remote IP address of the server.
     *
     * @return The remote IP address as a string.
     */
    @NotNull String getRemoteIp();

    /**
     * Gets a collection of voice player information for the connected players with Plasmo Voice installed.
     *
     * @return A collection of voice player information.
     */
    @NotNull Collection<VoicePlayerInfo> getPlayers();

    /**
     * Gets voice player information by player unique identifier.
     *
     * @param playerId The unique identifier of the player.
     * @return An optional containing the voice player information if found, or empty if not found.
     */
    Optional<VoicePlayerInfo> getPlayerById(@NotNull UUID playerId);

    /**
     * Gets the voice player information for the local player if the player is connected to Plasmo Voice.
     *
     * @return An optional containing the voice player information for the local player, or empty if Plasmo Voice is not connected.
     */
    Optional<VoicePlayerInfo> getLocalPlayer();

    /**
     * Sends a packet to the server with UDP connection check.
     *
     * @param packet The packet to send.
     */
    default void sendPacket(@NotNull Packet<?> packet) {
        sendPacket(packet, true);
    }

    /**
     * Sends a packet to the server.
     *
     * @param packet             The packet to send.
     * @param checkUdpConnection Whether to check the UDP connection before sending.
     */
    void sendPacket(@NotNull Packet<?> packet, boolean checkUdpConnection);

    /**
     * Closes and cleans up the server connection.
     */
    void close();

    /**
     * Gets the key pair associated with the server connection.
     * <br>
     * Key pair is only used to encrypt (on server) and decrypt (on client)
     * key data of the {@link su.plo.voice.proto.data.encryption.EncryptionInfo}
     *
     * @return The key pair.
     */
    @NotNull KeyPair getKeyPair();

    /**
     * Sets the key pair associated with the server connection.
     * <p>
     * Usually it generates automatically with {@link su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket} received,
     * but in some rare cases (like in <a href="https://github.com/plasmoapp/pv-addon-replaymod">pv-addon-replaymod</a>) it should be set manually.
     * </p>
     *
     * @param keyPair The key pair to set.
     */
    void setKeyPair(@NotNull KeyPair keyPair);

    /**
     * Gets the encryption information for the server connection.
     *
     * @return An optional containing the encryption information if available, or empty if server has disabled encryption.
     */
    Optional<EncryptionInfo> getEncryptionInfo();
}
