package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.cllientbound.ClientPacketHandler;

import java.util.UUID;

// todo: doc
public interface UdpConnection {
    UUID getSecret();

    VoicePlayer getPlayer();

    void sendPacket(Packet<ClientPacketHandler> packet);

    void disconnect();
}
