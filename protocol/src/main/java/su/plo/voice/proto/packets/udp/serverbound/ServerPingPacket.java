package su.plo.voice.proto.packets.udp.serverbound;

import su.plo.voice.proto.packets.udp.PingPacket;

public class ServerPingPacket extends PingPacket<ServerPacketHandler> {

    @Override
    public void handle(ServerPacketHandler listener) {
        listener.handle(this);
    }
}
