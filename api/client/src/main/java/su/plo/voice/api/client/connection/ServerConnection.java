package su.plo.voice.api.client.connection;

import su.plo.voice.proto.packets.Packet;

public interface ServerConnection {

    void sendPacket(Packet<?> packet);
}
