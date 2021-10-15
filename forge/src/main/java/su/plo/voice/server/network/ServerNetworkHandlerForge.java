package su.plo.voice.server.network;

import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.VoiceForge;
import su.plo.voice.client.network.VoiceNetworkPacket;
import su.plo.voice.common.packets.tcp.ClientConnectPacket;

import java.io.IOException;

public class ServerNetworkHandlerForge extends ServerNetworkHandler {
    public void register() {
        VoiceForge.CHANNEL.registerMessage(7, ClientConnectPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientConnectPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientConnectPacket()), (msg, ctx) -> {
                    try {
                        this.handle(msg, ctx.get().getSender());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void handleJoin(ServerPlayer player) {
        super.handleJoin(player);
        ServerNetworkHandler.reconnectClient(player);
    }
}
