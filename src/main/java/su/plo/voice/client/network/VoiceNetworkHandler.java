package su.plo.voice.client.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.VoiceServerConfig;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.socket.SocketClientUDP;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class VoiceNetworkHandler {
    public void handle(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(data);
            ByteArrayDataInput in = ByteStreams.newDataInput(data);

            Packet pkt = PacketTCP.read(in);
            if (pkt instanceof ServerConnectPacket packet) {
                VoiceClient.disconnect();
                VoiceClient.socketUDP = null;


                // so stupid but it works Pepega
                if (!(handler.getConnection().getRemoteAddress() instanceof InetSocketAddress)) {
                    return;
                }

                String ip = packet.getIp();

                InetSocketAddress addr = (InetSocketAddress) handler.getConnection().getRemoteAddress();
                Inet4Address in4addr = (Inet4Address) addr.getAddress();
                String[] ipSplit = in4addr.toString().split("/");

                String serverIp = ipSplit[0];
                if (ipSplit.length > 1) {
                    serverIp = ipSplit[1];
                }

                if (ip.equals("0.0.0.0")) {
                    ip = serverIp;
                }

                VoiceClient.LOGGER.info("Connecting to " + client.getCurrentServer().ip);

                VoiceClient.setServerConfig(new VoiceServerConfig(packet.getToken(), ip,
                        packet.getPort(), packet.hasPriority()));

                responseSender.sendPacket(new ServerboundCustomPayloadPacket(VoiceClient.PLASMO_VOICE,
                        new FriendlyByteBuf(Unpooled.wrappedBuffer(PacketTCP.write(new ClientConnectPacket(packet.getToken(), VoiceClient.PROTOCOL_VERSION))))));
            } else if (pkt instanceof ConfigPacket config) {
                if (VoiceClient.getServerConfig() != null) {
                    VoiceClient.getServerConfig().update(config);

                    if (!VoiceClient.isConnected()) {
                        VoiceClient.socketUDP = new SocketClientUDP();
                        VoiceClient.socketUDP.start();

                        if (client.screen instanceof VoiceNotAvailableScreen screen) {
                            screen.setConnecting();
                        }
                    }
                }
            } else if (pkt instanceof ClientMutedPacket muted) {
                if (VoiceClient.getServerConfig() != null) {
                    VoiceClient.getServerConfig().getMuted().put(muted.getClient(), new MutedEntity(muted.getClient(), muted.getTo()));
                    AbstractSoundQueue queue = SocketClientUDPQueue.audioChannels.get(muted.getClient());
                    if (queue != null) {
                        queue.closeAndKill();
                        SocketClientUDPQueue.audioChannels.remove(muted.getClient());
                    }

                    VoiceClient.LOGGER.info(muted.getClient().toString() + " muted");
                }
            } else if (pkt instanceof ClientUnmutedPacket unmuted) {
                if (VoiceClient.getServerConfig() != null) {
                    VoiceClient.getServerConfig().getMuted().remove(unmuted.getClient());

                    VoiceClient.LOGGER.info(unmuted.getClient().toString() + " unmuted");
                }
            } else if (pkt instanceof ClientsListPacket list) {
                if (VoiceClient.getServerConfig() != null) {
                    VoiceClient.getServerConfig().getMuted().clear();
                    VoiceClient.getServerConfig().getClients().clear();

                    List<String> mutedList = new ArrayList<>();

                    for (MutedEntity muted : list.getMuted()) {
                        mutedList.add(muted.uuid.toString());
                        VoiceClient.getServerConfig().getMuted().put(muted.uuid, muted);
                    }
                    VoiceClient.getServerConfig().getClients().addAll(list.getClients());

                    VoiceClient.LOGGER.info("Clients: " + list.getClients().toString());
                    VoiceClient.LOGGER.info("Muted clients: " + mutedList);
                }
            } else if (pkt instanceof ClientConnectedPacket connected) {
                if (VoiceClient.getServerConfig() != null) {
                    if (connected.getMuted() != null) {
                        MutedEntity muted = connected.getMuted();
                        VoiceClient.getServerConfig().getMuted().put(muted.uuid, muted);
                    }

                    VoiceClient.getServerConfig().getClients().add(connected.getClient());
                }
            } else if (pkt instanceof ClientDisconnectedPacket disconnected) {
                if (VoiceClient.getServerConfig() != null) {
                    VoiceClient.getServerConfig().getClients().remove(disconnected.getClient());
                    VoiceClient.getServerConfig().getMuted().remove(disconnected.getClient());

                    final LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        if (disconnected.getClient().equals(player.getUUID())) {
                            VoiceClient.disconnect();
                        }
                    }
                }
            }
        } catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
