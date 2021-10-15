package su.plo.voice.client.network;

import su.plo.voice.VoiceForge;
import su.plo.voice.common.packets.tcp.*;

import java.io.IOException;

public class ClientNetworkHandlerForge extends ClientNetworkHandler {
    public void register() {
        VoiceForge.CHANNEL.registerMessage(0, ClientsListPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientsListPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientsListPacket()), (msg, ctx) -> {
                    this.handle(msg);
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(1, ClientMutedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientMutedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientMutedPacket()), (msg, ctx) -> {
                    this.handle(msg);
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(2, ClientUnmutedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientUnmutedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientUnmutedPacket()), (msg, ctx) -> {
                    this.handle(msg);
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(3, ClientConnectedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientConnectedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientConnectedPacket()), (msg, ctx) -> {
                    this.handle(msg);
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(4, ClientDisconnectedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientDisconnectedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientDisconnectedPacket()), (msg, ctx) -> {
                    this.handle(msg);
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(5, ConfigPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ConfigPacket) VoiceNetworkPacket.readFromBuf(buf, new ConfigPacket()), (msg, ctx) -> {
                    try {
                        this.handle(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ctx.get().setPacketHandled(true);
                });

        VoiceForge.CHANNEL.registerMessage(6, ServerConnectPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ServerConnectPacket) VoiceNetworkPacket.readFromBuf(buf, new ServerConnectPacket()), (msg, ctx) -> {
                    try {
                        this.handle(msg, ctx.get().getNetworkManager());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ctx.get().setPacketHandled(true);
                });
    }
}
