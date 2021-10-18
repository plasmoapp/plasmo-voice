package su.plo.voice.client.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.local.LocalAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ServerSettings;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.socket.SocketClientUDP;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientNetworkHandler {
    private final Minecraft client = Minecraft.getInstance();

    public void reply(Connection connection, Packet packet) throws IOException {
        connection.send(
                new ServerboundCustomPayloadPacket(
                        VoiceClient.PLASMO_VOICE,
                        new FriendlyByteBuf(Unpooled.wrappedBuffer(PacketTCP.write(packet)))
                )
        );
    }

    public void handle(ServerConnectPacket packet, Connection connection) throws IOException {
        VoiceClient.disconnect();
        VoiceClient.socketUDP = null;

        if (!(connection.getRemoteAddress() instanceof InetSocketAddress) &&
                !(connection.getRemoteAddress() instanceof LocalAddress)) {
            return;
        }

        String ip = packet.getIp();
        String serverIp = "127.0.0.1";

        if (ip.equals("0.0.0.0")) {
            if (connection.getRemoteAddress() instanceof InetSocketAddress) {
                InetSocketAddress addr = (InetSocketAddress) connection.getRemoteAddress();
                Inet4Address in4addr = (Inet4Address) addr.getAddress();
                String[] ipSplit = in4addr.toString().split("/");

                serverIp = ipSplit[0];
                if (ipSplit.length > 1) {
                    serverIp = ipSplit[1];
                }
            }

            ip = serverIp;
        }

        VoiceClient.LOGGER.info("Connecting to " + (client.getCurrentServer() == null ? "localhost" : client.getCurrentServer().ip));

        VoiceClient.setServerConfig(new ServerSettings(packet.getToken(), ip,
                packet.getPort(), packet.hasPriority()));

        this.reply(connection, new ClientConnectPacket(packet.getToken(), VoiceClient.PROTOCOL_VERSION));
    }

    public void handle(ConfigPacket packet) throws IOException {
        if (VoiceClient.getServerConfig() != null) {
            VoiceClient.getServerConfig().update(packet);

            if (!VoiceClient.isConnected()) {
                VoiceClient.socketUDP = new SocketClientUDP();
                VoiceClient.socketUDP.start();

                if (client.screen instanceof VoiceNotAvailableScreen) {
                    ((VoiceNotAvailableScreen) client.screen).setConnecting();
                }
            }
        }
    }

    public void handle(ClientMutedPacket packet) {
        if (VoiceClient.isConnected()) {
            VoiceClient.getServerConfig().getMuted().put(packet.getClient(), new MutedEntity(packet.getClient(), packet.getTo()));
            AbstractSoundQueue queue = SocketClientUDPQueue.audioChannels.get(packet.getClient());
            if (queue != null) {
                queue.closeAndKill();
                SocketClientUDPQueue.audioChannels.remove(packet.getClient());
            }

            VoiceClient.LOGGER.info(packet.getClient().toString() + " muted");
        }
    }

    public void handle(ClientUnmutedPacket packet) {
        if (VoiceClient.isConnected()) {
            VoiceClient.getServerConfig().getMuted().remove(packet.getClient());

            VoiceClient.LOGGER.info(packet.getClient().toString() + " unmuted");
        }
    }

    public void handle(ClientsListPacket packet) {
        if (VoiceClient.getServerConfig() != null) { // check only config, because it can be sent before udp is connected
            VoiceClient.getServerConfig().getMuted().clear();
            VoiceClient.getServerConfig().getClients().clear();

            List<String> mutedList = new ArrayList<>();

            for (MutedEntity muted : packet.getMuted()) {
                mutedList.add(muted.uuid.toString());
                VoiceClient.getServerConfig().getMuted().put(muted.uuid, muted);
            }
            VoiceClient.getServerConfig().getClients().addAll(packet.getClients());

            VoiceClient.LOGGER.info("Clients: " + packet.getClients().toString());
            VoiceClient.LOGGER.info("Muted clients: " + mutedList);
        }
    }

    public void handle(ClientConnectedPacket packet) {
        if (VoiceClient.isConnected()) {
            if (packet.getMuted() != null) {
                MutedEntity muted = packet.getMuted();
                VoiceClient.getServerConfig().getMuted().put(muted.uuid, muted);
            }

            VoiceClient.getServerConfig().getClients().add(packet.getClient());
        }
    }

    public void handle(ClientDisconnectedPacket packet) {
        if (VoiceClient.isConnected()) {
            VoiceClient.getServerConfig().getClients().remove(packet.getClient());
            VoiceClient.getServerConfig().getMuted().remove(packet.getClient());

            final LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (packet.getClient().equals(player.getUUID())) {
                    VoiceClient.disconnect();
                }
            }
        }
    }
}
