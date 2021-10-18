package su.plo.voice.server.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.ClientConnectPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;

public class ServerNetworkHandlerForge extends ServerNetworkHandler {
    public void handle(ServerPlayer player, FriendlyByteBuf buf) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(data);
            ByteArrayDataInput in = ByteStreams.newDataInput(data);

            Packet pkt = PacketTCP.read(in);
            if (pkt instanceof ClientConnectPacket) {
                ClientConnectPacket packet = (ClientConnectPacket) pkt;
                this.handle(packet, player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleJoin(ServerPlayer player) {
        super.handleJoin(player);
        ServerNetworkHandler.reconnectClient(player);
    }
}
