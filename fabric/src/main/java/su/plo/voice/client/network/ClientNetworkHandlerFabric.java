package su.plo.voice.client.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;

public class ClientNetworkHandlerFabric extends ClientNetworkHandler {
    public void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(data);
            ByteArrayDataInput in = ByteStreams.newDataInput(data);

            Packet pkt = PacketTCP.read(in);
            // not good implementation, but with the current protocol (v1.0.0) I can't do something with it
            if (pkt instanceof ServerConnectPacket) {
                ServerConnectPacket packet = (ServerConnectPacket) pkt;
                this.handle(packet, handler.getConnection());
            } else if (pkt instanceof ConfigPacket) {
                ConfigPacket packet = (ConfigPacket) pkt;
                this.handle(packet);
            } else if (pkt instanceof ClientMutedPacket) {
                ClientMutedPacket packet = (ClientMutedPacket) pkt;
                this.handle(packet);
            } else if (pkt instanceof ClientUnmutedPacket) {
                ClientUnmutedPacket packet = (ClientUnmutedPacket) pkt;
                this.handle(packet);
            } else if (pkt instanceof ClientsListPacket) {
                ClientsListPacket packet = (ClientsListPacket) pkt;
                this.handle(packet);
            } else if (pkt instanceof ClientConnectedPacket) {
                ClientConnectedPacket packet = (ClientConnectedPacket) pkt;
                this.handle(packet);
            } else if (pkt instanceof ClientDisconnectedPacket) {
                ClientDisconnectedPacket packet = (ClientDisconnectedPacket) pkt;
                this.handle(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
