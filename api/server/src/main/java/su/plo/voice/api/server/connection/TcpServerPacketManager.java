package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.clientbound.*;

/**
 * Used to send Plasmo Voice related TCP packets to the players.
 */
public interface TcpServerPacketManager extends PacketManager<ClientPacketTcpHandler, VoiceServerPlayer> {

    /**
     * Sends a {@link ConnectionPacket} to the player.
     *
     * @param receiver The player.
     */
    void connect(@NotNull VoiceServerPlayer receiver);

    /**
     * Sends a {@link PlayerInfoRequestPacket} to the player.
     *
     * @param receiver The player.
     */
    void requestPlayerInfo(@NotNull VoiceServerPlayer receiver);

    /**
     * Sends a {@link ConfigPacket} to the player.
     *
     * @param receiver The player.
     */
    void sendConfigInfo(@NotNull VoiceServerPlayer receiver);

    /**
     * Sends a {@link PlayerListPacket} to the player to provide the list of connected players.
     *
     * @param receiver The player who will receive the player list.
     */
    void sendPlayerList(@NotNull VoiceServerPlayer receiver);

    /**
     * Broadcasts a {@link PlayerInfoUpdatePacket} to all connected players to update player information.
     *
     * @param player The player whose information is being updated.
     */
    void broadcastPlayerInfoUpdate(@NotNull VoiceServerPlayer player);

    /**
     * Broadcasts a {@link PlayerDisconnectPacket} to all connected players
     * to inform them about a player's disconnection from the PV.
     *
     * @param player The player who has disconnected.
     */
    void broadcastPlayerDisconnect(@NotNull VoiceServerPlayer player);
}
