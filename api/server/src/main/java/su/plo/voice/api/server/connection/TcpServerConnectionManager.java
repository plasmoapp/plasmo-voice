package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;

// todo: doc
// todo: move to PlayerManager?
public interface TcpServerConnectionManager extends ConnectionManager<ClientPacketTcpHandler, VoiceServerPlayer> {

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket} to the player
     *
     * @param player the player
     */
    void connect(@NotNull VoiceServerPlayer player);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket} to the player
     *
     * @param receiver the player
     */
    void sendConfigInfo(@NotNull VoiceServerPlayer receiver);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket} to the player
     *
     * @param receiver the player
     */
    void sendPlayerList(@NotNull VoiceServerPlayer receiver);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket} to all connected players
     *
     * @param player the player
     */
    void broadcastPlayerInfoUpdate(@NotNull VoiceServerPlayer player);
}
