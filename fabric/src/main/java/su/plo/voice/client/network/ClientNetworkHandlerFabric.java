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
            if (pkt instanceof ServerConnectPacket packet) {
                this.handle(packet, handler.getConnection());
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
