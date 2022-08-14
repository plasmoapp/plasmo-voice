package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;

// todo: doc
public interface TcpServerConnectionManager extends ServerConnectionManager<ClientPacketTcpHandler> {

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket} to the player
     * @param player the player
     */
    void connect(@NotNull VoicePlayer player);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket} to the player
     *
     * @param player the player
     */
    void sendConfigInfo(@NotNull VoicePlayer player);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket} to the player
     *
     * @param player the player
     */
    void sendPlayerList(@NotNull VoicePlayer player);

    /**
     * Sends {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket} to all connected players
     */
    void sendPlayerInfoUpdate(@NotNull VoicePlayer player);
}
