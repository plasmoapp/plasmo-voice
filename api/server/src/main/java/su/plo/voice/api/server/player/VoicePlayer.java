package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;

import java.util.UUID;

/**
 * Represents "proxy" player to an actual server player object
 */
public interface VoicePlayer {

    /**
     * Gets the player's unique id
     */
    @NotNull UUID getUUID();

    /**
     * Gets the backed player object
     */
    <T> T getServerPlayer();

    /**
     * Sends a packet to the player
     */
    void sendPacket(Packet<ClientPacketTcpHandler> packet);
}
