package su.plo.voice.client.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;

public class ClientNetworkHandlerForge extends ClientNetworkHandler {
    public void handle(Connection connection, FriendlyByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(data);
            ByteArrayDataInput in = ByteStreams.newDataInput(data);

            Packet pkt = PacketTCP.read(in);
            // not good implementation, but with the current protocol (v1.0.0) I can't do something with it
            if (pkt instanceof ServerConnectPacket packet) {
                this.handle(packet, connection);
            } else if (pkt instanceof ConfigPacket packet) {
                this.handle(packet);
            } else if (pkt instanceof ClientMutedPacket packet) {
                this.handle(packet);
            } else if (pkt instanceof ClientUnmutedPacket packet) {
                this.handle(packet);
            } else if (pkt instanceof ClientsListPacket packet) {
                this.handle(packet);
            } else if (pkt instanceof ClientConnectedPacket packet) {
                this.handle(packet);
            } else if (pkt instanceof ClientDisconnectedPacket packet) {
                this.handle(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
